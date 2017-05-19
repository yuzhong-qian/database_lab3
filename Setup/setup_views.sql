-- CREATE USER IF NOT EXISTS 'editor'@'localhost' IDENTIFIED BY 'cs61';
-- CREATE USER IF NOT EXISTS 'author'@'localhost' IDENTIFIED BY 'cs61';
-- CREATE USER IF NOT EXISTS 'reviewer'@'localhost' IDENTIFIED BY 'cs61';

-- GRANT ALL PRIVILEGES ON `cuteqian_db`.* TO 'editor'@'localhost';
-- GRANT INSERT, SELECT, UPDATE, DELETE ON `cuteqian_db`.`Author` TO 'author'@'localhost';
-- GRANT INSERT, SELECT, UPDATE, DELETE ON `cuteqian_db`.`Manuscript` TO 'author'@'localhost';
-- GRANT SELECT ON `cuteqian_db`.`RICodes` TO 'author'@'localhost';
-- GRANT INSERT, SELECT, UPDATE, DELETE ON `cuteqian_db`.`Reviewer` TO 'reviewer'@'localhost';
-- GRANT INSERT, SELECT, UPDATE, DELETE ON `cuteqian_db`.`Feedback` TO 'reviewer'@'localhost';
-- GRANT INSERT, SELECT, UPDATE, DELETE ON `cuteqian_db`.`Assignment` TO 'reviewer'@'localhost';
-- GRANT SELECT ON `cuteqian_db`.`RICodes` TO 'reviewer'@'localhost';
-- GRANT SELECT ON `cuteqian_db`.`AnyAuthorManuscripts` TO 'author'@'localhost';
-- GRANT SELECT ON `cuteqian_db`.`PublishedIssues` TO 'author'@'localhost', 'reviewer'@'localhost';
-- GRANT SELECT ON `cuteqian_db`.`ReviewStatus` TO 'reviewer'@'localhost';

DROP VIEW IF EXISTS LeadAuthorManuscripts;

CREATE VIEW LeadAuthorManuscripts AS
SELECT `au`.`authorLastName`, `au`.`authorFirstName`, `au`.`mailAddress`, `au`.`emailAddress`, `au`.`affliation`, `ma`.`idManuscript`, `ma`.`title`, `ma`.`status`, `ma`.`date`
FROM `cuteqian_db`.`Author` AS `au` 
JOIN `cuteqian_db`.`Manuscript` AS `ma` ON au.idAuthor = ma.idAuthor
ORDER BY `au`.`authorLastName`, `ma`.`date`;

DROP VIEW IF EXISTS AnyAuthorManuscripts;

CREATE VIEW AnyAuthorManuscripts AS
SELECT `au`.`authorLastName`, `au`.`authorFirstName`, `ma`.`idManuscript`, `ma`.`title`, `ma`.`status`, `ma`.`date`
FROM `cuteqian_db`.`Author` AS `au` 
JOIN `cuteqian_db`.`Manuscript` AS `ma` ON `ma`.`authorList` LIKE CONCAT('%', `au`.`authorFirstName`, ' ',  `au`.`authorLastName`, '%') 
ORDER BY `au`.`authorLastName`, `ma`.`date`;

DROP VIEW IF EXISTS PublishedIssues;

CREATE VIEW PublishedIssues AS
SELECT `issue`.`publicationYear`, `issue`.`publicationPeriod`, `ma`.`title`, `ts`.`beginPage`
FROM `cuteqian_db`.`Typesetting` AS `ts`
JOIN `cuteqian_db`.`Issue` AS `issue` ON `issue`.`publicationYear` = `ts`.`publicationYear` and `issue`.`publicationPeriod` = `ts`.`publicationPeriod`
JOIN `cuteqian_db`.`Manuscript` AS `ma` ON `ma`.`idManuscript` = `ts`.`idManuscript`
WHERE `issue`.`printDate` IS NOT NULL
ORDER BY `issue`.`publicationYear`, `issue`.`publicationPeriod`, `ts`.`beginPage`;

DROP VIEW IF EXISTS ReviewQueue;

CREATE VIEW ReviewQueue AS
SELECT `au`.`authorLastName`, `au`.`authorFirstName`, `ma`.`idManuscript`, `re`.`reviewerLastName`, `re`.`reviewerFirstName`, `ma`.`date`
FROM `cuteqian_db`.`Assignment` AS `am`
JOIN `cuteqian_db`.`Manuscript` AS `ma` ON `ma`.`idManuscript` = `am`.`idManuscript`
JOIN `cuteqian_db`.`Reviewer` AS `re` ON `re`.`idReviewer` = `am`.`idReviewer`
JOIN `cuteqian_db`.`Author` AS `au` ON `ma`.`idAuthor` = `au`.`idAuthor`
WHERE `ma`.`status` = 'Under review'
ORDER BY `ma`.`date`;

DROP FUNCTION IF EXISTS nextStep;

DELIMITER $$

CREATE FUNCTION nextStep(currStatus varchar(45)) RETURNS VARCHAR(45)
DETERMINISTIC
BEGIN
 DECLARE nextStatus VARCHAR(45);
    IF currStatus = 'Under review' THEN SET nextStatus = 'Either reject or accept';
    ELSEIF currStatus = 'Accepted' THEN SET nextStatus = 'In typesetting';
    ELSEIF currStatus = 'In typesetting' THEN SET nextStatus = 'Scheduled for publication';
    ELSEIF currStatus = 'Scheduled for publication' THEN SET nextStatus = 'published';
 ELSEIF currStatus = 'Submitted' THEN SET nextStatus = 'Either Rejected or under review';
    
    END IF;
RETURN(nextStatus);
END $$
DELIMITER ;

DROP VIEW IF EXISTS WhatIsNext;

CREATE VIEW WhatIsNext AS
SELECT  `manu`.`idManuscript`,`manu`.`title`, `manu`.`status`, nextStep(`manu`.`status`) AS nextStep
FROM `cuteqian_db`.`Manuscript` AS `manu`;


DROP VIEW IF EXISTS ReviewStatus;

CREATE VIEW ReviewStatus AS
SELECT `am`.`assignDate`, `ma`.`date` as `submission date`, `ma`.`idManuscript`, `am`.`idReviewer` ,`ma`.`title`, `fe`.`appropriateness`, `fe`.`clarity`, `fe`.`methodology`, `fe`.`contribution`, `fe`.`recommendation`
FROM `cuteqian_db`.`Assignment` AS `am`
JOIN `cuteqian_db`.`Manuscript` AS `ma` ON `ma`.`idManuscript` = `am`.`idManuscript`
LEFT JOIN `cuteqian_db`.`Feedback` AS `fe` ON `fe`.`idFeedback` = `am`.`idFeedback`
ORDER BY `ma`.`date`;

