#!/bin/bash

# DataValidationStepDefinitions.java
sed -i '
s/public void яСоздаюРезюмеСИменемДлиной/public void iCreateResumeWithNameLength/g
s/public void резюмеДолжноБытьОтклонено/public void resumeShouldBeRejected/g
s/public void должноПоявитьсяСообщение/public void messageShouldAppear/g
s/public void яСоздаюРезюмеСИменем/public void iCreateResumeWithName/g
s/public void резюмеДолжноБытьСозданоУспешно/public void resumeShouldBeCreatedSuccessfully/g
s/public void имяДолжноБытьСохраненоКак/public void nameShouldBeSavedAs/g
s/public void яДобавляюEmail/public void iAddEmail/g
s/public void яДобавляюТелефон/public void iAddPhone/g
s/public void телефонДолженБытьПринят/public void phoneShouldBeAccepted/g
s/public void уМеняЕстьРезюме/public void iHaveResume/g
s/public void яДобавляюПериодРаботыС/public void iAddWorkPeriodFrom/g
s/public void должнаПоявитьсяОшибка/public void errorShouldAppear/g
s/public void периодНеДолженБытьДобавлен/public void periodShouldNotBeAdded/g
s/public void яСоздаюНовыхРезюме/public void iCreateNewResumes/g
s/public void должноБытьСозданоНеБолееРезюме/public void shouldCreateNotMoreThanResumes/g
s/public void приПревышенииЛимитаДолжнаБытьОшибка/public void shouldHaveErrorWhenLimitExceeded/g
' /srv/java_09/webapp/src/test/java/webapp/cucumber/DataValidationStepDefinitions.java

# IntegrationStepDefinitions.java
sed -i '
s/public void новыйПользовательРегистрируется/public void newUserRegisters/g
s/public void должноБытьОтправленоПисьмоПодтверждения/public void confirmationEmailShouldBeSent/g
s/public void письмоДолжноСодержать/public void emailShouldContain/g
s/public void пользовательПереходитПоСсылкеАктивации/public void userFollowsActivationLink/g
s/public void аккаунтДолженБытьАктивирован/public void accountShouldBeActivated/g
s/public void яЗагружаюФотоПрофиля/public void iUploadProfilePhoto/g
s/public void фотоДолжноБытьЗагруженоВS3/public void photoShouldBeUploadedToS3/g
s/public void должнаБытьСозданаМиниатюра/public void thumbnailShouldBeCreated/g
s/public void долженБытьВозвращенПубличныйURL/public void publicUrlShouldBeReturned/g
s/public void яУдаляюРезюме/public void iDeleteResume/g
s/public void связанныеФайлыДолжныБытьУдаленыИзХранилища/public void relatedFilesShouldBeDeletedFromStorage/g
s/public void происходятСобытия/public void eventsOccur/g
s/public void событияДолжныБытьОтправленыВGoogleAnalytics/public void eventsShouldBeSentToGoogleAnalytics/g
s/public void должныСодержатьМетаданные/public void shouldContainMetadata/g
s/public void яДобавляюПериодРаботыСДатами/public void iAddWorkPeriodWithDates/g
s/public void включаюСинхронизациюСКалендарем/public void iEnableCalendarSync/g
s/public void должноБытьСозданоСобытиеВGoogleCalendar/public void eventShouldBeCreatedInGoogleCalendar/g
s/public void событиеДолжноСодержать/public void eventShouldContain/g
s/public void происходитВажноеСобытие/public void importantEventOccurs/g
s/public void должныБытьОтправленыУведомленияЧерез/public void notificationsShouldBeSentVia/g
s/public void наступаетВремяРезервногоКопирования/public void backupTimeArrives/g
s/public void долженБытьСозданБэкапВсехРезюме/public void backupShouldBeCreatedForAllResumes/g
s/public void бэкапДолженБытьЗашифрован/public void backupShouldBeEncrypted/g
s/public void загруженВОблачноеХранилище/public void uploadedToCloudStorage/g
s/public void должнаБытьПроверенаЦелостность/public void integrityShouldBeChecked/g
s/public void старыеБэкапыДолжныБытьУдаленыСогласноПолитике/public void oldBackupsShouldBeDeletedByPolicy/g
s/public void hrСистемаЗапрашиваетРезюмеЧерезAPI/public void hrSystemRequestsResumeViaAPI/g
s/public void долженБытьПредоставленДоступПоOAuth/public void oauthAccessShouldBeProvided/g
s/public void данныеДолжныБытьОтданыВФормате/public void dataShouldBeProvidedInFormat/g
s/public void яНажимаю/public void iClick/g
s/public void долженБытьСозданПостС/public void postShouldBeCreatedWith/g
s/public void должнаБытьВозможностьКастомизацииПоста/public void postCustomizationShouldBeEnabled/g
' /srv/java_09/webapp/src/test/java/webapp/cucumber/IntegrationStepDefinitions.java

echo "Method renaming completed!"