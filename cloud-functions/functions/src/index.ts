import * as logger from "firebase-functions/logger";
import { Firestore } from "@google-cloud/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import * as admin from "firebase-admin";
import {
  onDocumentCreated,
  onDocumentDeleted,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";

const firestore = new Firestore();

admin.initializeApp({
  credential: admin.credential.cert("./service-account.json"),
  storageBucket: "gs://momentum-8a9dc.firebasestorage.app",
});

/**
 * Sends a notification to the specified FCM token.
 * @param {Object} params - The notification parameters.
 * @param {string} params.title - The title of the notification.
 * @param {string} params.body - The body of the notification.
 * @param {string} params.fcmToken - The FCM token to send the notification to.
 */
async function sendNotification({
  title,
  body,
  fcmToken,
}: {
  title: string;
  body: string;
  fcmToken: string;
}) {
  if (!fcmToken) {
    logger.info("No FCM token found for user");
    return;
  }
  const message = {
    token: fcmToken,
    notification: { title, body },
  };
  try {
    const response = await admin.messaging().send(message);
    logger.info("Notification sent successfully", response);
  } catch (error) {
    logger.error("Failed to send notification", error);
  }
}

export const onChainCreated = onDocumentCreated(
  "chains/{chainId}",
  async (event) => {
    const chainData = event.data?.data() as any;
    if (!chainData) {
      logger.error("No chain data found");
      return;
    }

    try {
      // Convert the data to match your Chain model with default values
      const chain = {
        id: event.data?.id ?? null,
        theme: chainData.theme ?? "",
        createdBy: chainData.createdBy ?? null,
        participants: chainData.participants ?? [],
        deadline: chainData.deadline ?? null,
        streak: chainData.streak ?? 0,
        status: chainData.status ?? "active", // Add appropriate default status
        photos: chainData.photos ?? [],
        chainStatus: chainData.chainStatus ?? "active", // Add appropriate default chainStatus
      };

      // Validate required fields
      if (!chain.id || !chain.createdBy || !chain.participants.length) {
        logger.error("Missing required chain data", chain);
        return;
      }

      // Add the chain to each participant's user_chains collection
      const batch = firestore.batch();

      for (const participantId of chain.participants) {
        const userChainRef = firestore
          .collection("user_chains")
          .doc(participantId)
          .collection("user_chains")
          .doc(chain.id!);
        batch.set(userChainRef, chain);
      }

      await batch.commit();
      logger.info(
        `Chain ${chain.id} added to ${chain.participants.length} participants`
      );

      // Send notifications to all participants
      const participantsSnapshot = await firestore
        .collection("users")
        .where("uid", "in", chain.participants)
        .get();

      const notificationPromises = participantsSnapshot.docs.map(
        async (userDoc) => {
          const userData = userDoc.data();
          if (userData.fcmToken && userData.uid !== chain.createdBy) {
            await sendNotification({
              title: "New Chain Started!",
              body: `You've been added to a new chain: ${chain.theme}`,
              fcmToken: userData.fcmToken,
            });
          }
        }
      );

      await Promise.all(notificationPromises);
      logger.info(`Notifications sent for chain ${chain.id}`);
    } catch (error) {
      logger.error("Error processing new chain:", error);
    }
  }
);

export const onChainDeleted = onDocumentDeleted(
  "chains/{chainId}",
  async (event) => {
    const chainData = event.data?.data() as any;
    if (!chainData) {
      logger.error("No chain data found for deletion");
      return;
    }

    try {
      // Create a batch for deleting user_chains
      const batch = firestore.batch();

      // Delete the chain from each participant's user_chains collection
      for (const participantId of chainData.participants) {
        const userChainRef = firestore
          .collection("user_chains")
          .doc(participantId)
          .collection("user_chains")
          .doc(event.data!.id!);

        batch.delete(userChainRef);
      }

      await batch.commit();
      logger.info(
        `Chain ${event.data?.id} removed from 
        ${chainData.participants.length} participants`
      );
    } catch (error) {
      logger.error("Error processing chain deletion:", error);
    }
  }
);

export const onChainUpdated = onDocumentUpdated(
  "chains/{chainId}",
  async (event) => {
    const updatedChainData = event.data?.after.data() as any;
    if (!updatedChainData) {
      logger.error("No updated chain data found");
      return;
    }

    try {
      const chain = {
        id: event.data?.after.id ?? null,
        theme: updatedChainData.theme ?? "",
        createdBy: updatedChainData.createdBy ?? null,
        participants: updatedChainData.participants ?? [],
        deadline: updatedChainData.deadline ?? null,
        streak: updatedChainData.streak ?? 0,
        status: updatedChainData.status ?? "active",
        photos: updatedChainData.photos ?? [],
        chainStatus: updatedChainData.chainStatus ?? "active",
      };

      // Validate required fields
      if (!chain.id || !chain.createdBy || !chain.participants.length) {
        logger.error("Missing required chain data", chain);
        return;
      }

      // Update the chain in each participant's user_chains collection
      const batch = firestore.batch();

      for (const participantId of chain.participants) {
        const userChainRef = firestore
          .collection("user_chains")
          .doc(participantId)
          .collection("user_chains")
          .doc(chain.id);

        batch.set(userChainRef, chain, { merge: true });
      }

      await batch.commit();
      logger.info(
        `Chain ${chain.id} updated for ${chain.participants.length} participants`
      );
    } catch (error) {
      logger.error("Error processing chain update:", error);
    }
  }
);

export const cleanupExpiredChains = onSchedule(
  "0 */12 * * *",
  async (event) => {
    try {
      const now = Date.now();

      // Get all chains where deadline has passed
      const expiredChains = await firestore
        .collection("chains")
        .where("deadline", "<", now)
        .get();

      if (expiredChains.empty) {
        logger.info("No expired chains found");
        return;
      }

      // Create a batch for deleting chains
      const batch = firestore.batch();

      // Process each expired chain
      for (const doc of expiredChains.docs) {
        const chain = doc.data();

        // Delete the main chain document
        batch.delete(doc.ref);

        // Queue cleanup of user_chains for each participant
        for (const participantId of chain.participants) {
          const userChainRef = firestore
            .collection("user_chains")
            .doc(participantId)
            .collection("user_chains")
            .doc(doc.id);

          batch.delete(userChainRef);
        }
      }

      await batch.commit();
      logger.info(`Deleted ${expiredChains.size} expired chains`);
    } catch (error) {
      logger.error("Error cleaning up expired chains:", error);
    }
  }
);
