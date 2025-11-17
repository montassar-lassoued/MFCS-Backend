CREATE TABLE Mitgliederrrrrrrrrrrrrrrrrrrrr (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Vorname NVARCHAR(100) NOT NULL,
    Nachname NVARCHAR(100) NOT NULL,
    Geburtsdatum DATE,
    MitgliedSeit DATE DEFAULT GETDATE()
);