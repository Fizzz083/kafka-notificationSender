package models

import play.api.libs.json.Json

case class EmailRequest(
                       val emailId: Int,
                       val status: String,
                       val responseId: String,
                       val requestId: Int
                       )
//object EmailRequest{
//  implicit  val stockImplicitRead = Json.reads[EmailRequest]
//  implicit  val stockImplicitWrite = Json.writes[EmailRequest]
//}
case class EmailRequestDbModel(
                                val status: String,
                                val responseId: String,
                                val requestId: Int
                              )


//-- create table emailRequest (
//--     emailId int auto_increment not null,
//--     status varchar(10) not null,
//--     resonseId varchar(200) null,
//--     requestId int not null,
//--     primary key (emailId),
//--     foreign key (requestId) references notificationRequest(requestId)
//-- );