CREATE TABLE LoadUnit (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    number VARCHAR(100) UNIQUE,
    created DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    description VARCHAR(100)
);

CREATE TABLE Article (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) UNIQUE,
    length INT NULL,
    width INT NULL,
    height INT NULL,
    weight INT NULL,
    unit VARCHAR(10),
    description VARCHAR(100) NULL
);

CREATE TABLE LoadUnit_Article (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loadUnit_ID BIGINT UNIQUE NOT NULL,
    article_ID BIGINT NOT NULL
);

CREATE TABLE LoadUnit_Roadway (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loadUnit_ID BIGINT UNIQUE NOT NULL,
    state VARCHAR(100) NOT NULL,
    origin VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    nextLocation VARCHAR(100),
    destination VARCHAR(100) NOT NULL
);

CREATE TABLE Journal_Roadway (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loadUnit_ID BIGINT NOT NULL,
    origin VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    nextLocation VARCHAR(100),
    destination VARCHAR(100) NOT NULL,
    created DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

-- Constraints
ALTER TABLE [dbo].[LoadUnit_Roadway] WITH CHECK ADD CONSTRAINT [FK_LoadUnit_Roadway] FOREIGN KEY([loadUnit_ID])
REFERENCES [dbo].[LoadUnit]([id]);

ALTER TABLE [dbo].[LoadUnit_Article] WITH CHECK ADD CONSTRAINT [FK_LoadUnit_Article_1] FOREIGN KEY([loadUnit_ID])
REFERENCES [dbo].[LoadUnit]([id]);

ALTER TABLE [dbo].[LoadUnit_Article] WITH CHECK ADD CONSTRAINT [FK_LoadUnit_Article_2] FOREIGN KEY([article_ID])
REFERENCES [dbo].[Article]([id]);

ALTER TABLE [dbo].[Journal_Roadway] WITH CHECK ADD CONSTRAINT [FK_Journal_Roadway] FOREIGN KEY([loadUnit_ID])
REFERENCES [dbo].[LoadUnit]([id]);