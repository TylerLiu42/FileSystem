insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('', '', '', 'directory', 1, 1, 1, NULL, NULL);

insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('1', 'root', '', 'directory', 1, 1, 1, NULL, NULL);

insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID) VALUES ('2', 'IntelliJ', '1', 'directory', 1, 1, 1, NULL);

insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission) VALUES ('3', 'src', '2', 'directory', 1, 1, 1);

insert into Content VALUES (1, 'TODO', 10);
insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('4', 'main.java', '3', 'hardLink', 1, 1, 1, 1, NULL);

insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission) VALUES ('5', 'Photos', '1', 'directory', 1, 1, 1);

insert into Content VALUES (2, 'abcdef', 500);
insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('6', 'lol.jpg', '5', 'hardLink', 1, 1, 1, 2, NULL);

insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('7', 'rel1', '5', 'symLink', 1, 1, 1, NULL, "./..");
insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('8', 'rel2', '5', 'symLink', 1, 1, 1, NULL, "root");
insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('9', 'abs', '5', 'symLink', 1, 1, 1, NULL, "/root/IntelliJ/src");
insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('10', 'cy1', '2', 'symLink', 1, 1, 1, NULL, "cy2");
insert into File (fileID, name, parent, fileType, readPermission, writePermission, execPermission, contentID, symLink) VALUES ('11', 'cy2', '2', 'symLink', 1, 1, 1, NULL, "cy1");
