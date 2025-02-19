const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendReminderNotification = functions.firestore
    .document("reminders/{reminderId}")
    .onCreate(async (snapshot, context) => {
      const data = snapshot.data();
      const userId = data.userId;
      const title = data.name;
      const message = "Time to record your accounts!";

      try {
        const userRef = admin.firestore().collection("users").doc(userId);
        const userDoc = await userRef.get();

        if (userDoc.exists && userDoc.data().fcmToken) {
          const token = userDoc.data().fcmToken;

          const payload = {
            notification: {
              title: title,
              body: message,
            },
          };

          await admin.messaging().sendToDevice(token, payload);
          console.log("Notification sent to:", token);
        }
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    });
