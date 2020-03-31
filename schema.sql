create table Content (
    contentID VARCHAR(36) not null,
    data BLOB not null,
    size bigint not null,

    PRIMARY KEY (contentID)
);

create table File (
    fileID VARCHAR(36) not null,
    name VARCHAR(256) not null,
    parent VARCHAR(36) not null,  -- root directory points to itself
    fileType ENUM('directory', 'hardLink', 'symLink') not null,
    readPermission boolean not null,
    writePermission boolean not null,
    execPermission boolean not null,
    created timestamp not null default current_timestamp,
    lastAccessed timestamp not null default current_timestamp,
    lastModified timestamp not null default current_timestamp,
    contentID VARCHAR(36),
    symLink VARCHAR(4096),

    PRIMARY KEY (fileID),
    FOREIGN KEY (contentID) references Content(contentID),
    FOREIGN KEY (parent) references File(fileID),

    -- ensures (parent, name) uniqueness, even if parent is null
    File_path VARCHAR(300) GENERATED ALWAYS as (concat_ws( '?', name, ifnull(parent, '') )) virtual not null unique,

    CHECK((fileID like '') or (parent != fileID))
);

-- need to ensure in app that:
-- parents are directories
-- sym links have the symLink field
-- only regular files and hardlinks point to content
-- unlinked Content is deleted
