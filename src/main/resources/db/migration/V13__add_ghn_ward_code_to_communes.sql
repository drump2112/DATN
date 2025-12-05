-- Add GHN Ward Code and District ID columns to Communes table for shipping fee calculation
ALTER TABLE Communes
ADD GHNWardCode VARCHAR(20) NULL,
    GHNDistrictId INT NULL;

-- Add indexes for faster lookup
CREATE INDEX idx_communes_ghn_ward_code ON Communes(GHNWardCode);
CREATE INDEX idx_communes_ghn_district_id ON Communes(GHNDistrictId);

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'GHN Ward Code for shipping fee API integration',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'Communes',
    @level2type = N'COLUMN', @level2name = N'GHNWardCode';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'GHN District ID for shipping fee API integration',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'Communes',
    @level2type = N'COLUMN', @level2name = N'GHNDistrictId';
