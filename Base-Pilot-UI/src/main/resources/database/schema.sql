

CREATE TABLE AppUsers (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255),
    state VARCHAR(255),
    ROLE_ID BIGINT
);
CREATE TABLE ROLE (
    id BIGINT PRIMARY KEY,
    role VARCHAR(5),
    description VARCHAR(255),
);






-- CONSTRAINT--
ALTER TABLE [dbo].[AppUsers]  WITH CHECK ADD  CONSTRAINT [FK_AppUsers] FOREIGN KEY([ROLE_ID])
REFERENCES [dbo].[ROLE] ([ID]);
ALTER TABLE [dbo].[AppUsers] CHECK CONSTRAINT [FK_AppUsers];