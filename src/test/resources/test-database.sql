CREATE TABLE EpnPerson (
  eierkode                  VARCHAR(10)     NOT NULL,
  brukernavn                VARCHAR(30)     NULL,
  fodselsdato               NUMERIC(6,0)    NOT NULL,
  personnr                  NUMERIC(5,0)    NOT NULL,
  fornavn                   VARCHAR(30)     NOT NULL,
  etternavn                 VARCHAR(30)     NOT NULL,
  emailadresse              VARCHAR(100)    NULL,
  dato_Fodt                 DATE            NOT NULL,
  institusjonsnr_ansatt     NUMERIC(8,0)    NULL,
  faknr_ansatt              NUMERIC(2,0)    NULL,
  instituttnr_ansatt        NUMERIC(2,0)    NULL,
  gruppenr_ansatt           NUMERIC(2,0)    NULL,
  status_saksbehandler		VARCHAR(1)		NOT NULL);

CREATE TABLE EpnBrukerinfo (
  eierkode                  VARCHAR(10)     NOT NULL,
  brukernavn                VARCHAR(60)     NOT NULL,
  fodselsnummer             VARCHAR(11)     NOT NULL,
  fornavn                   VARCHAR(30)     NULL,
  etternavn                 VARCHAR(30)     NULL,
  emailadresse              VARCHAR(100)    NOT NULL,
  dato_sist_innlogget       DATE            NULL,
  epnrollekode              VARCHAR(10)     NOT NULL,
  epnrollekode_foretrukket  VARCHAR(10)     NULL);

--Ubrukte kolonner i EpnPerson:
-- status_fagperson          VARCHAR(1)      NOT NULL,
-- status_administrator      VARCHAR(1)      NOT NULL,
  
--Constraints tatt fra SQL Developer, bør aktiveres her:
-- ALTER TABLE Epn.EpnBrukerinfo ADD CONSTRAINT brukernavn_EpnBrukerinfo CHECK (brukernavn = UPPER(brukernavn)) DISABLE ALTER TABLE Epn.EpnBrukerinfo ADD CONSTRAINT eierk_EpnBrukerinfo CHECK (eierkode = UPPER(eierkode)) ENABLE ALTER TABLE Epn.EpnBrukerinfo ADD CONSTRAINT epnrollekode_EpnBrukerinfo CHECK (epnrollekode = UPPER(epnrollekode)) ENABLE ALTER TABLE Epn.EpnBrukerinfo ADD CONSTRAINT epnrollekode_f_EpnBrukerinfo CHECK (epnrollekode_foretrukket = UPPER(epnrollekode_foretrukket)) ENABLECREATE TABLE Epn.EpnBrukerinfo ( eierkode VARCHAR2(10 BYTE) NOT NULL , brukernavn VARCHAR2(60 BYTE) NOT NULL , fodselsnummer VARCHAR2(11 BYTE) NOT NULL , emailadresse VARCHAR2(100 BYTE) NOT NULL , dato_sist_innlogget DATE , personnavn VARCHAR2(100 BYTE) , epnrollekode VARCHAR2(10 BYTE) NOT NULL , epnrollekode_foretrukket VARCHAR2(10 BYTE) , fornavn VARCHAR2(30 BYTE) , etternavn VARCHAR2(30 BYTE) , CONSTRAINT I01_EpnBrukerinfo PRIMARY KEY ( eierkode , brukernavn ) ENABLE ) LOGGING TABLESPACE
-- "EPN_DATA" PCTFREE 10 INITRANS 1 STORAGE ( INITIAL 32768 NEXT 65536 MINEXTENTS 1 MAXEXTENTS 2147483645 BUFFER_POOL DEFAULT )ALTER TABLE Epn.EpnBrukerinfo ADD CONSTRAINT epnrolle_EpnBrukerinfo FOREIGN KEY ( eierkode , epnrollekode ) REFERENCES Epn.epnrolle ( eierkode , epnrollekode ) ENABLE ALTER TABLE Epn.EpnBrukerinfo ADD CONSTRAINT epnrolle_f_EpnBrukerinfo FOREIGN KEY ( eierkode , epnrollekode_foretrukket ) REFERENCES Epn.EPNROLLE ( eierkode , epnrollekode ) ENABLE

--Eksempel på innlegging av data (se src/test/java/no/usit/epn/controller/BrukerRolleAdminControllerUnitTest.java):
-- INSERT INTO EpnBrukerinfo (eierkode, brukernavn, fodselsnummer, emailadresse, fornavn, etternavn, epnrollekode)
--  VALUES ('UIO', 'joser', '22113300268', 'j.l.rojas@usit.uio.no', 'Jose Luis', 'Rojas', 'ADMIN')