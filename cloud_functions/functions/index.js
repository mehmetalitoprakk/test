const functions = require("firebase-functions");
const admin = require('firebase-admin');
admin.initializeApp();

exports.grupBildirimiGonder=functions.database.ref("/grupkonusmalar/{user_id}/{grup_key}/son_mesaj").onWrite(async (change,context) =>{
    const userId = context.params.user_id;
    const grupKey = context.params.grup_key;
    const mesaj = change.after.val();
    console.log(`user id : ${userId}  grupKey : ${grupKey}, mesaj : ${mesaj}`);

    const grupName = admin.database().ref(`/groups/${grupKey}/groupName`).once('value')
    const token = admin.database().ref(`/users/${userId}/fcmToken`).once('value')

    return token.then(result =>{
        const bildirimAtilacakUserToken = result.val();
        return grupName.then(result =>{
            const bildirimAtilacakGrupName = result.val();

            const grupBildirimi = {
                notification : {
                    title : `${bildirimAtilacakGrupName}`,
                    body : `${mesaj}`,
                    icon : 'defalut'
                }
            };
            return admin.messaging().sendToDevice(bildirimAtilacakUserToken,grupBildirimi).then(result =>{
                console.log(`grup bildirimi gönderildi`)
            });
        });
    });
});





exports.yeniMesajBildirimiGonder=functions.database.ref("/chats/{mesaj_atan_user_id}/{mesaj_alan_user_id}/{mesaj_key}").onCreate((data,context)=>{
    const mesajAtanUserId = context.params.mesaj_atan_user_id;
    const mesajAlanUserId = context.params.mesaj_alan_user_id;
    const mesajKey = context.params.mesaj_key;

    console.log("Mesaj atan user id : ",mesajAtanUserId);
    console.log("Mesaj alan user id : ",mesajAlanUserId);
    console.log("Mesaj Key : ",mesajKey);


    const token = admin.database().ref(`/users/${mesajAlanUserId}/fcmToken`).once('value');
    const name =  admin.database().ref(`/users/${mesajAtanUserId}/isim`).once('value');
    const surname = admin.database().ref(`/users/${mesajAtanUserId}/soyisim`).once('value');
    const mesajType = admin.database().ref(`chats/${mesajAtanUserId}/${mesajAlanUserId}/${mesajKey}/type`).once('value')
    const mesajText = admin.database().ref(`chats/${mesajAtanUserId}/${mesajAlanUserId}/${mesajKey}/mesaj`).once('value')
    const sonMesajAtanUserId = admin.database().ref(`chats/${mesajAtanUserId}/${mesajAlanUserId}/${mesajKey}/user_id`).once('value');

    return token.then(result=>{
        const mesajAlanUserFcmToken = result.val();
        return name.then(result=>{
            const mesajAtanUserName = result.val();
            return surname.then(result =>{
                const mesajAtanUserSurname = result.val();
                return mesajType.then(result =>{
                    const mesajValueType = result.val();
                    return mesajText.then(result =>{
                        const mesajValueText = result.val();
                        return sonMesajAtanUserId.then(result=>{
                            const user_id = result.val();
                            if(user_id == mesajAtanUserId){

                                if(mesajValueType == 'text'){
                                    const mesajBildirimi = {
                                        notification : {
                                            title : `${mesajAtanUserName} ${mesajAtanUserSurname}`,
                                            body : `${mesajValueText}`,
                                            icon : 'default'
                                        }
                                    };
                                    return admin.messaging().sendToDevice(mesajAlanUserFcmToken,mesajBildirimi).then(result =>{
                                        console.log("mesaj bildirimi gönderildi")
                                    });
                                    
                                }else if(mesajValueType == 'video'){
                                    const mesajBildirimi = {
                                        notification : {
                                            title : `${mesajAtanUserName} ${mesajAtanUserSurname}`,
                                            body : `Video`,
                                            icon : 'default'
                                        }
                                    };
                                    return admin.messaging().sendToDevice(mesajAlanUserFcmToken,mesajBildirimi).then(result =>{
                                        console.log("mesaj bildirimi gönderildi")
                                    });
        
                                }else if(mesajValueType == 'image'){
                                    const mesajBildirimi = {
                                        notification : {
                                            title : `${mesajAtanUserName} ${mesajAtanUserSurname}`,
                                            body : `Fotoğraf`,
                                            icon : 'default'
                                        }
                                    };
                                    return admin.messaging().sendToDevice(mesajAlanUserFcmToken,mesajBildirimi).then(result =>{
                                        console.log("mesaj bildirimi gönderildi")
                                    });
                                }
                            }
                        });
                    });
                });
            });
        });
    });
});


