CREATE TABLE A7_FORM (ID VARCHAR(46) NOT NULL, CREATED_AT DATETIME, CREATED_BY VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), REG_NUM VARCHAR(20) UNIQUE, STATUS INTEGER, VERSION INTEGER, OWNER_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE INDEX INDEX_A7_FORM_OWNER_ID_STATUS_REG_NUM ON A7_FORM (OWNER_ID, STATUS, REG_NUM)
CREATE TABLE CONTACT (ID VARCHAR(46) NOT NULL, TYPE VARCHAR(31), CELL_PHONE VARCHAR(20), CREATED_AT DATETIME, CREATED_BY VARCHAR(20), EMAIL VARCHAR(35), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), NAME VARCHAR(50), VERSION INTEGER, CITY VARCHAR(15), POST_INDEX VARCHAR(6), REGION VARCHAR(20), STREET_BLD VARCHAR(255), AFFILIATION_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE INDEX INDEX_CONTACT_NAME ON CONTACT (NAME)
CREATE INDEX INDEX_CONTACT_TYPE_NAME ON CONTACT (TYPE, NAME)
CREATE TABLE COMPANY (ID VARCHAR(46) NOT NULL, FULL_NAME VARCHAR(50), PRIMARY KEY (ID))
CREATE INDEX INDEX_COMPANY_FULL_NAME ON COMPANY (FULL_NAME)
CREATE TABLE CONTACT_CODE (ID VARCHAR(46) NOT NULL, CODE VARCHAR(35), CREATED_AT DATETIME, CREATED_BY VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), TYPE VARCHAR(20), VERSION INTEGER, CONTACT_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE UNIQUE INDEX INDEX_CONTACT_CODE_TYPE_CODE ON CONTACT_CODE (TYPE, CODE)
CREATE TABLE FORM_TRANSFER (ID VARCHAR(46) NOT NULL, CREATED_AT DATETIME, CREATED_BY VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), TRANSFERDATE DATE, VERSION INTEGER, FROMCONTACT_ID VARCHAR(46), TOCONTACT_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE TABLE INSURANCE (ID VARCHAR(46) NOT NULL, A7_NUM VARCHAR(20) UNIQUE, CREATED_AT DATETIME, CREATED_BY VARCHAR(20), `DATE` DATE, END_DATE DATE, MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), MOTOR_BRAND VARCHAR(20), MOTOR_MODEL VARCHAR(20), MOTOR_TYPE VARCHAR(20), PAYMENT_DATE DATE, POINT_OF_SALE VARCHAR(255), PREMIUM DECIMAL(38), REG_NUM VARCHAR(20) UNIQUE, RISK_SUM DECIMAL(38), START_DATE DATE, VERSION INTEGER, CLIENT_ID VARCHAR(46), DEALER_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE INDEX INDEX_INSURANCE_REG_NUM ON INSURANCE (REG_NUM)
CREATE INDEX INDEX_INSURANCE_A7_NUM ON INSURANCE (A7_NUM)
CREATE INDEX INDEX_INSURANCE_"DATE" ON INSURANCE ("DATE")
CREATE TABLE LEAD (ID VARCHAR(46) NOT NULL, COMMENT VARCHAR(255), CONTACT_EMAIL VARCHAR(255), CONTACT_NAME VARCHAR(255), CONTACT_PHONE VARCHAR(255), CREATED_AT DATETIME, CREATED_BY VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), MOTOR_BRAND VARCHAR(255), MOTOR_MODEL VARCHAR(255), MOTOR_PRICE DECIMAL(38), MOTOR_TYPE VARCHAR(255), POINT_OF_SALE VARCHAR(255), REGION VARCHAR(255), STATUS VARCHAR(255), VERSION INTEGER, CLIENT_ID VARCHAR(46), VENDOR_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE TABLE SALE (ID VARCHAR(46) NOT NULL, COMMENT VARCHAR(255), CREATED_AT DATETIME, CREATED_BY VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), MOTOR_BRAND VARCHAR(255), MOTOR_MODEL VARCHAR(255), MOTOR_PRICE DECIMAL(38), MOTOR_TYPE VARCHAR(255), REGION VARCHAR(255), STATUS VARCHAR(255), TYPE VARCHAR(255), VERSION INTEGER, CLIENT_ID VARCHAR(46), DEALER_ID VARCHAR(46), VENDOR_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE TABLE PAY_ACCOUNT (ID VARCHAR(46) NOT NULL, BANK_CODE VARCHAR(35), BANK_NAME VARCHAR(50), CREATED_AT DATETIME, CREATED_BY VARCHAR(20), LORO_ACCOUNT VARCHAR(35), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), SETTLEMENT_ACCOUNT VARCHAR(35), VERSION INTEGER, CONTACT_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE TABLE PERSON (ID VARCHAR(46) NOT NULL, BIRTHDAY DATE, JOB_DEPARTMENT VARCHAR(50), JOB_POSITION VARCHAR(15), PASS_ISSUE_DATE DATE, PASS_ISSUED_BY VARCHAR(255), PASS_ISSUED_BY_NUM VARCHAR(10), PASS_NUM VARCHAR(30), SEX VARCHAR(6), PRIMARY KEY (ID))
CREATE TABLE POLICY (ID VARCHAR(46) NOT NULL, BOOK_TIME DATETIME, CREATED_AT DATETIME, CREATED_BY VARCHAR(20), ISSUE_DATE DATETIME, MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), REG_NUM VARCHAR(20) UNIQUE, VERSION INTEGER, PRIMARY KEY (ID))
CREATE INDEX INDEX_POLICY_ISSUE_DATE_BOOK_TIME_REG_NUM ON POLICY (ISSUE_DATE, BOOK_TIME, REG_NUM)
CREATE TABLE USER_GROUP (ID VARCHAR(46) NOT NULL, CREATED_AT DATETIME, CREATED_BY VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), NAME VARCHAR(50), VERSION INTEGER, PRIMARY KEY (ID))
CREATE TABLE USER_PROFILE (ID VARCHAR(46) NOT NULL, BLOCKED TINYINT(1) default 0, CHANGE_PASSWORD TINYINT(1) default 0, CREATED_AT DATETIME, CREATED_BY VARCHAR(20), LOGIN VARCHAR(20), MODIFIED_AT DATETIME, MODIFIED_BY VARCHAR(20), PASSWORD VARCHAR(255), PASSWORD_SALT VARCHAR(26), ROLE VARCHAR(255), VERSION INTEGER, CONTACT_ID VARCHAR(46), PRIMARY KEY (ID))
CREATE TABLE FORM_TRANSFER_NUMS (FORM_TRANSFER_ID VARCHAR(46), FORMNUMS VARCHAR(255))
CREATE INDEX INDEX_FORM_TRANSFER_NUMS_FORM_TRANSFER_ID_FORMNUMS ON FORM_TRANSFER_NUMS (FORM_TRANSFER_ID, FORMNUMS)
CREATE TABLE USER_GROUP_PERMISSION (UserGroup_ID VARCHAR(46), PERMISSIONLIST VARCHAR(255))
CREATE TABLE USER_GROUP_LINK (UserProfile_ID VARCHAR(46) NOT NULL, groupList_ID VARCHAR(46) NOT NULL, PRIMARY KEY (UserProfile_ID, groupList_ID))
ALTER TABLE A7_FORM ADD CONSTRAINT FK_A7_FORM_OWNER_ID FOREIGN KEY (OWNER_ID) REFERENCES CONTACT (ID)
ALTER TABLE CONTACT ADD CONSTRAINT FK_CONTACT_AFFILIATION_ID FOREIGN KEY (AFFILIATION_ID) REFERENCES CONTACT (ID)
ALTER TABLE COMPANY ADD CONSTRAINT FK_COMPANY_ID FOREIGN KEY (ID) REFERENCES CONTACT (ID)
ALTER TABLE CONTACT_CODE ADD CONSTRAINT FK_CONTACT_CODE_CONTACT_ID FOREIGN KEY (CONTACT_ID) REFERENCES CONTACT (ID)
ALTER TABLE FORM_TRANSFER ADD CONSTRAINT FK_FORM_TRANSFER_FROMCONTACT_ID FOREIGN KEY (FROMCONTACT_ID) REFERENCES CONTACT (ID)
ALTER TABLE FORM_TRANSFER ADD CONSTRAINT FK_FORM_TRANSFER_TOCONTACT_ID FOREIGN KEY (TOCONTACT_ID) REFERENCES CONTACT (ID)
ALTER TABLE INSURANCE ADD CONSTRAINT FK_INSURANCE_CLIENT_ID FOREIGN KEY (CLIENT_ID) REFERENCES CONTACT (ID)
ALTER TABLE INSURANCE ADD CONSTRAINT FK_INSURANCE_DEALER_ID FOREIGN KEY (DEALER_ID) REFERENCES CONTACT (ID)
ALTER TABLE LEAD ADD CONSTRAINT FK_LEAD_VENDOR_ID FOREIGN KEY (VENDOR_ID) REFERENCES CONTACT (ID)
ALTER TABLE LEAD ADD CONSTRAINT FK_LEAD_CLIENT_ID FOREIGN KEY (CLIENT_ID) REFERENCES CONTACT (ID)
ALTER TABLE SALE ADD CONSTRAINT FK_SALE_CLIENT_ID FOREIGN KEY (CLIENT_ID) REFERENCES CONTACT (ID)
ALTER TABLE SALE ADD CONSTRAINT FK_SALE_VENDOR_ID FOREIGN KEY (VENDOR_ID) REFERENCES CONTACT (ID)
ALTER TABLE SALE ADD CONSTRAINT FK_SALE_DEALER_ID FOREIGN KEY (DEALER_ID) REFERENCES CONTACT (ID)
ALTER TABLE PAY_ACCOUNT ADD CONSTRAINT FK_PAY_ACCOUNT_CONTACT_ID FOREIGN KEY (CONTACT_ID) REFERENCES CONTACT (ID)
ALTER TABLE PERSON ADD CONSTRAINT FK_PERSON_ID FOREIGN KEY (ID) REFERENCES CONTACT (ID)
ALTER TABLE USER_PROFILE ADD CONSTRAINT FK_USER_PROFILE_CONTACT_ID FOREIGN KEY (CONTACT_ID) REFERENCES CONTACT (ID)
ALTER TABLE FORM_TRANSFER_NUMS ADD CONSTRAINT FK_FORM_TRANSFER_NUMS_FORM_TRANSFER_ID FOREIGN KEY (FORM_TRANSFER_ID) REFERENCES FORM_TRANSFER (ID)
ALTER TABLE USER_GROUP_PERMISSION ADD CONSTRAINT FK_USER_GROUP_PERMISSION_UserGroup_ID FOREIGN KEY (UserGroup_ID) REFERENCES USER_GROUP (ID)
ALTER TABLE USER_GROUP_LINK ADD CONSTRAINT FK_USER_GROUP_LINK_groupList_ID FOREIGN KEY (groupList_ID) REFERENCES USER_GROUP (ID)
ALTER TABLE USER_GROUP_LINK ADD CONSTRAINT FK_USER_GROUP_LINK_UserProfile_ID FOREIGN KEY (UserProfile_ID) REFERENCES USER_PROFILE (ID)
