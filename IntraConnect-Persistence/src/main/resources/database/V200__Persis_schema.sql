
CREATE TABLE Controller (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    connected bit
);

CREATE TABLE Transfer_In (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    controller_ID BIGINT,
    _date date,
    content VARBINARY(MAX),
    processed VARCHAR(10)
);

CREATE TABLE Transfer_Out (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    controller_ID BIGINT,
    _date date,
    content VARBINARY(MAX),
    processed VARCHAR(10)
);

CREATE TABLE ChangeEventListener(
	id BIGINT IDENTITY(1,1) PRIMARY KEY,
	table_name VARCHAR(50),
    operation VARCHAR(10),
    entity_id BIGINT,
    processed BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);

-- CONSTRAINT--
ALTER TABLE [dbo].[Transfer_In]  WITH CHECK ADD  CONSTRAINT [FK_Transfer_In] FOREIGN KEY([controller_ID])
REFERENCES [dbo].[Controller] ([ID]);
ALTER TABLE [dbo].[Transfer_In] CHECK CONSTRAINT [FK_Transfer_In];

ALTER TABLE [dbo].[Transfer_Out]  WITH CHECK ADD  CONSTRAINT [FK_Transfer_Out] FOREIGN KEY([controller_ID])
REFERENCES [dbo].[Controller] ([ID]);
ALTER TABLE [dbo].[Transfer_Out] CHECK CONSTRAINT [FK_Transfer_Out];

CREATE INDEX idx_change_log_processed
ON ChangeEventListener(processed);