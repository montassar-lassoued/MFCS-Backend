
CREATE OR ALTER TRIGGER dbo.trg_LoadUnit_change
ON dbo.LoadUnit
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.ChangeEventListener(table_name, operation, entity_id)
    SELECT 'LoadUnit', 'INSERT', i.id
    FROM inserted i
    LEFT JOIN deleted d ON i.id = d.id
    WHERE d.id IS NULL;

    INSERT INTO dbo.ChangeEventListener(table_name, operation, entity_id)
    SELECT 'LoadUnit', 'DELETE', d.id
    FROM deleted d
    LEFT JOIN inserted i ON i.id = d.id
    WHERE i.id IS NULL;

    INSERT INTO dbo.ChangeEventListener(table_name, operation, entity_id)
    SELECT 'LoadUnit', 'UPDATE', i.id
    FROM inserted i
    INNER JOIN deleted d ON i.id = d.id;
END;