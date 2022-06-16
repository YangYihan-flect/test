public with sharing class CMN_SendEmail {
  // フロー入力用クラス
  @TestVisible
  public class FlowInput {
    @InvocableVariable(label='送信先アドレスのリスト' required=true)
    public List<String> toAddressList;

    @InvocableVariable(label='メールの件名' required=true)
    public String subject;

    @InvocableVariable(label='メールの本文' required=true)
    public String body;

    @InvocableVariable(label='添付ファイルのIdのリスト' description='ContentDocumentIdのリスト')
    public List<Id> contentDocumentIdList;

    @InvocableVariable(label='ファイルの添付先レコードのId')
    public Id linkedEntityId;
  }

  // メール送信結果クラス
  @TestVisible
  public class EmailResult {
    @InvocableVariable
    public Boolean isSuccess;

    @InvocableVariable
    public String message;
  }

  /**
   * フローから呼び出すメール送信
   */
  @InvocableMethod(label='Apexでのメール送信')
  public static List<EmailResult> sendEmail_Flow(List<FlowInput> params) {
    System.debug('★' + 'params : ' + params);
    FlowInput param = params[0];

    List<Messaging.EmailFileAttachment> attachments = getAttachments(
      param.contentDocumentIdList
      , param.linkedEntityId
    );

    EmailResult er = sendEmail(
      param.toAddressList
      , param.subject
      , param.body
      , attachments
    );

    return new List<EmailResult>{er};
  }

  /**
   * 他クラスから呼び出すメール送信（添付ファイルなし）
   * @param toAddress 送信先アドレスのリスト
   * @param subject メール件名
   * @param body メール本文
   */
  public static EmailResult sendEmail(List<String> toAddressList, String subject, String body) {

    EmailResult er = sendEmail(
      toAddressList
      , subject
      , body
      , new List<Messaging.EmailFileAttachment>()
    );

    return er;
  }

  /**
   * 他クラスから呼び出すメール送信（添付ファイルあり）
   * @param toAddress 送信先アドレスのリスト
   * @param subject メール件名
   * @param body メール本文
   * @param contentDocumentIdList メールに添付したいContentDocumentのIdのリスト
   * @param linkedEntityId ファイルを添付しているレコードのId
   */
  public static EmailResult sendEmail(List<String> toAddressList, String subject, String body, List<Id> contentDocumentIdList, Id linkedEntityId) {

    List<Messaging.EmailFileAttachment> attachments = getAttachments(
      contentDocumentIdList
      , linkedEntityId
    );
    EmailResult er = sendEmail(
      toAddressList
      , subject
      , body
      , attachments
    );

    return er;
  }

  /**
   * メール送信処理
   */
  private static EmailResult sendEmail(List<String> toAddress, String subject, String body, List<Messaging.EmailFileAttachment> attachments) {
    Messaging.SingleEmailMessage email = new Messaging.SingleEmailMessage();
    email.setToAddresses(toAddress);
    email.setSubject(subject);
    email.setPlainTextBody(body);
    if (attachments.size() > 0) {
        email.setFileAttachments(attachments);
    }
    
    EmailResult er = new EmailResult();
    try {
      Messaging.sendEmail(new Messaging.SingleEmailMessage[]{email});
      er.isSuccess = true;
    } catch(Exception e) {
      er.isSuccess = false;
      er.message = e.getMessage();
    }
    return er;
  }

  /**
   * 添付ファイル取得
   */
  private static List<Messaging.EmailFileAttachment> getAttachments(List<Id> contentDocumentIdList, Id linkedEntityId) {
    List<Messaging.EmailFileAttachment> attachments = new List<Messaging.EmailFileAttachment>();
    if (contentDocumentIdList.isEmpty() || String.isEmpty(linkedEntityId)) {
      return attachments;
    }
    
    // 添付ファイルの取得
    List<ContentDocumentLink> cdlList = [
      SELECT
        Id
        , ContentDocumentId
        , ContentDocument.LatestPublishedVersion.VersionData
        , ContentDocument.Title
        , ContentDocument.FileExtension
      FROM
        ContentDocumentLink
      WHERE
        LinkedEntityId = :linkedEntityId
    ];
    
    for (ContentDocumentLink cdl : cdlList) {
      if (contentDocumentIdList.contains(cdl.ContentDocumentId)) {
        Messaging.EmailFileAttachment attach = new Messaging.EmailFileAttachment();
        Blob b = cdl.ContentDocument.LatestPublishedVersion.VersionData;
        attach.setFileName(cdl.ContentDocument.Title + '.' + cdl.ContentDocument.FileExtension);
        attach.setBody(b);
        attachments.add(attach);
      }
    }
    return attachments;
  }
}
