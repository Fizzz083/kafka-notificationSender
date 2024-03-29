package models


case class NotificationRequest (
                  val requestId: Int,
                  val requestType: String,
                  val requestDetails: String,
                  val requestDate: String
                 )
case class NotificationRequestDbModel(
                  val requestType:String,
                  val requestDetails:String,
                  val requestDate: String
                )

//-- create table notificationRequest (
//--     requestId int auto_increment not null,
//--     requestType varchar(10) not null,
//--     requestDetails varchar(2000) not null,
//--     requestDate date not null,
//--     primary key (requestId)
//-- );