
CREATE OR ALTER TRIGGER dbo.trg_Roadway_Update
ON dbo.LoadUnit_Roadway
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

	--INSERT
    INSERT INTO dbo.Journal_Roadway(loadUnit_ID, origin, location, nextLocation, destination)
    SELECT i.loadUnit_ID, i.origin, i.location, i.nextLocation, i.destination
    FROM inserted i
    LEFT JOIN deleted d ON i.id = d.id
    WHERE d.id IS NULL;
	--UPDATE
    INSERT INTO dbo.Journal_Roadway(loadUnit_ID, origin, location, nextLocation, destination)
    SELECT i.loadUnit_ID, i.origin, i.location, i.nextLocation, i.destination
    FROM inserted i
    INNER JOIN deleted d ON i.id = d.id;
END;
