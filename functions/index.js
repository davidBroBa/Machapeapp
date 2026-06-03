const {onValueWritten} =
require("firebase-functions/v2/database");

const admin =
require("firebase-admin");

admin.initializeApp();

exports.sendAttentionNotification =
onValueWritten(
    "/touch/{roomCode}",

    async (event) => {

        const data =
            event.data.after.val();

        if (!data) return;

        if (
            data.text !==
            "need_attention"
        ) {
            return;
        }

        const roomCode =
            event.params.roomCode;

        const tokensRef =
            admin.database().ref(
                `/tokens/${roomCode}`
            );

        const snapshot =
            await tokensRef.once("value");

        const tokens =
            snapshot.val();

        if (!tokens) return;

        const sender =
            data.sender;

        const promises = [];

        Object.keys(tokens)
            .forEach((id) => {

                if (id !== sender) {

                    const message = {

                        token: tokens[id],

                        notification: {
                            title:
                                "Tu machape te necesita 🥺",

                            body:
                                "Alguien quiere tu atención 💜"
                        },

                        android: {
                            priority: "high"
                        }
                    };

                    promises.push(
                        admin.messaging()
                            .send(message)
                    );
                }
            });

        return Promise.all(promises);
    }
);