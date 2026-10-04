/* ====================================================================
   EduFrame Database Relational Schema DDL Script for Microsoft SQL Server
   Course: IT2140 Database Design and Development (2026 Y2-S1)
   Team: SLIIT Group B4G1-06 (Adikari A. M. T. D. et al.)
   ==================================================================== */

IF DB_ID('EduFrame-db') IS NULL
BEGIN
    CREATE DATABASE [EduFrame-db];
END
GO

USE [EduFrame-db];
GO

-- 1. Users (Base table for ISA hierarchy)
IF OBJECT_ID('dbo.Users', 'U') IS NULL
CREATE TABLE dbo.Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FirstName NVARCHAR(100) NOT NULL,
    LastName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(150) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RegisteredDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ProfilePicture NVARCHAR(500) NULL,
    Role NVARCHAR(20) NOT NULL CHECK (Role IN ('Student', 'Teacher', 'Admin')),
    CONSTRAINT UQ_User_Role UNIQUE (UserID, Role)
);
GO

-- 2. Student (ISA Subclass)
IF OBJECT_ID('dbo.Student', 'U') IS NULL
CREATE TABLE dbo.Student (
    UserID INT PRIMARY KEY,
    Role NVARCHAR(20) NOT NULL DEFAULT 'Student' CHECK (Role = 'Student'),
    EnrollmentYear INT NOT NULL CHECK (EnrollmentYear BETWEEN 2000 AND 2100),
    CONSTRAINT FK_Student_Users FOREIGN KEY (UserID, Role) REFERENCES dbo.Users(UserID, Role) ON DELETE CASCADE
);
GO

-- 3. Teacher (ISA Subclass)
IF OBJECT_ID('dbo.Teacher', 'U') IS NULL
CREATE TABLE dbo.Teacher (
    UserID INT PRIMARY KEY,
    Role NVARCHAR(20) NOT NULL DEFAULT 'Teacher' CHECK (Role = 'Teacher'),
    Department NVARCHAR(100) NOT NULL,
    Bio NVARCHAR(1000) NULL,
    CONSTRAINT FK_Teacher_Users FOREIGN KEY (UserID, Role) REFERENCES dbo.Users(UserID, Role) ON DELETE CASCADE
);
GO

-- 4. Admin (ISA Subclass)
IF OBJECT_ID('dbo.Admin', 'U') IS NULL
CREATE TABLE dbo.Admin (
    UserID INT PRIMARY KEY,
    Role NVARCHAR(20) NOT NULL DEFAULT 'Admin' CHECK (Role = 'Admin'),
    CONSTRAINT FK_Admin_Users FOREIGN KEY (UserID, Role) REFERENCES dbo.Users(UserID, Role) ON DELETE CASCADE
);
GO

-- 5. CourseCategory
IF OBJECT_ID('dbo.CourseCategory', 'U') IS NULL
CREATE TABLE dbo.CourseCategory (
    CategoryID INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- 6. Course
IF OBJECT_ID('dbo.Course', 'U') IS NULL
CREATE TABLE dbo.Course (
    CourseID INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    Description NVARCHAR(2000) NULL,
    CoverImage NVARCHAR(500) NULL,
    CategoryID INT NOT NULL,
    TeacherID INT NOT NULL,
    CONSTRAINT FK_Course_Category FOREIGN KEY (CategoryID) REFERENCES dbo.CourseCategory(CategoryID),
    CONSTRAINT FK_Course_Teacher FOREIGN KEY (TeacherID) REFERENCES dbo.Teacher(UserID)
);
GO

-- 7. Enrolls
IF OBJECT_ID('dbo.Enrolls', 'U') IS NULL
CREATE TABLE dbo.Enrolls (
    StudentID INT NOT NULL,
    CourseID INT NOT NULL,
    EnrollDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (StudentID, CourseID),
    CONSTRAINT FK_Enrolls_Student FOREIGN KEY (StudentID) REFERENCES dbo.Student(UserID),
    CONSTRAINT FK_Enrolls_Course FOREIGN KEY (CourseID) REFERENCES dbo.Course(CourseID)
);
GO

-- 8. Module
IF OBJECT_ID('dbo.Module', 'U') IS NULL
CREATE TABLE dbo.Module (
    ModuleID INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    OrderIndex INT NOT NULL CHECK (OrderIndex > 0),
    CourseID INT NOT NULL,
    CONSTRAINT UQ_Module_Course_Order UNIQUE (CourseID, OrderIndex),
    CONSTRAINT FK_Module_Course FOREIGN KEY (CourseID) REFERENCES dbo.Course(CourseID) ON DELETE CASCADE
);

-- 8b. AppUser (Quiz & Assessment User Table)
IF OBJECT_ID('dbo.app_user', 'U') IS NULL
CREATE TABLE dbo.app_user (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    role NVARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN')),
    createdAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

-- 9. Quiz
IF OBJECT_ID('dbo.quiz', 'U') IS NULL
CREATE TABLE dbo.quiz (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(150) NOT NULL,
    description NVARCHAR(1000) NULL,
    course_id BIGINT NULL,
    timer_minutes INT NOT NULL CHECK (timer_minutes >= 1),
    pass_mark_percentage INT NOT NULL CHECK (pass_mark_percentage >= 0),
    status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
    created_by BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    CONSTRAINT FK_Quiz_CreatedBy FOREIGN KEY (created_by) REFERENCES dbo.app_user(id)
);
GO

-- 10. Question
IF OBJECT_ID('dbo.question', 'U') IS NULL
CREATE TABLE dbo.question (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text NVARCHAR(1000) NOT NULL,
    question_type NVARCHAR(20) NOT NULL CHECK (question_type IN ('MCQ', 'TRUE_FALSE', 'SHORT_ANSWER')),
    marks INT NOT NULL CHECK (marks >= 1),
    correct_answer NVARCHAR(500) NOT NULL,
    sequence INT NULL,
    CONSTRAINT FK_Question_Quiz FOREIGN KEY (quiz_id) REFERENCES dbo.quiz(id) ON DELETE CASCADE
);
GO

-- 11. QuestionOption
IF OBJECT_ID('dbo.question_option', 'U') IS NULL
CREATE TABLE dbo.question_option (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text NVARCHAR(300) NOT NULL,
    sequence INT NULL,
    CONSTRAINT FK_Option_Question FOREIGN KEY (question_id) REFERENCES dbo.question(id) ON DELETE CASCADE
);
GO

-- 12. QuizAttempt
IF OBJECT_ID('dbo.quiz_attempt', 'U') IS NULL
CREATE TABLE dbo.quiz_attempt (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    started_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    submitted_at DATETIME2 NULL,
    score_obtained INT NULL,
    score_percentage INT NULL,
    passed BIT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'GRADED')),
    CONSTRAINT FK_Attempt_Quiz FOREIGN KEY (quiz_id) REFERENCES dbo.quiz(id),
    CONSTRAINT FK_Attempt_Student FOREIGN KEY (student_id) REFERENCES dbo.app_user(id)
);
GO

-- 12b. StudentAnswer
IF OBJECT_ID('dbo.student_answer', 'U') IS NULL
CREATE TABLE dbo.student_answer (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text NVARCHAR(1000) NULL,
    correct BIT NULL,
    marks_awarded INT NULL,
    CONSTRAINT FK_StudentAnswer_Attempt FOREIGN KEY (attempt_id) REFERENCES dbo.quiz_attempt(id) ON DELETE CASCADE,
    CONSTRAINT FK_StudentAnswer_Question FOREIGN KEY (question_id) REFERENCES dbo.question(id)
);
GO

-- 13. Certificate
IF OBJECT_ID('dbo.certificate', 'U') IS NULL
CREATE TABLE dbo.certificate (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    certificate_code NVARCHAR(50) NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    attempt_id BIGINT NOT NULL UNIQUE,
    issued_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    pdf_content VARBINARY(MAX) NULL,
    CONSTRAINT FK_Certificate_Student FOREIGN KEY (student_id) REFERENCES dbo.app_user(id),
    CONSTRAINT FK_Certificate_Quiz FOREIGN KEY (quiz_id) REFERENCES dbo.quiz(id),
    CONSTRAINT FK_Certificate_Attempt FOREIGN KEY (attempt_id) REFERENCES dbo.quiz_attempt(id)
);
GO

-- 14. Video
IF OBJECT_ID('dbo.Video', 'U') IS NULL
CREATE TABLE dbo.Video (
    VideoID INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    Description NVARCHAR(2000) NULL,
    Thumbnail NVARCHAR(500) NULL,
    FileURL NVARCHAR(500) NOT NULL,
    Duration INT NOT NULL CHECK (Duration > 0),
    UploadDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    TeacherID INT NOT NULL,
    ModuleID INT NULL,
    CONSTRAINT FK_Video_Teacher FOREIGN KEY (TeacherID) REFERENCES dbo.Teacher(UserID),
    CONSTRAINT FK_Video_Module FOREIGN KEY (ModuleID) REFERENCES dbo.Module(ModuleID)
);
GO

-- 15. Chapter
IF OBJECT_ID('dbo.Chapter', 'U') IS NULL
CREATE TABLE dbo.Chapter (
    VideoID INT NOT NULL,
    ChapterNo INT NOT NULL,
    ChapterTitle NVARCHAR(200) NOT NULL,
    StartTime INT NOT NULL CHECK (StartTime >= 0),
    PRIMARY KEY (VideoID, ChapterNo),
    CONSTRAINT FK_Chapter_Video FOREIGN KEY (VideoID) REFERENCES dbo.Video(VideoID) ON DELETE CASCADE
);
GO

-- 16. Caption
IF OBJECT_ID('dbo.Caption', 'U') IS NULL
CREATE TABLE dbo.Caption (
    VideoID INT NOT NULL,
    Language NVARCHAR(50) NOT NULL,
    CaptionFileURL NVARCHAR(500) NOT NULL,
    PRIMARY KEY (VideoID, Language),
    CONSTRAINT FK_Caption_Video FOREIGN KEY (VideoID) REFERENCES dbo.Video(VideoID) ON DELETE CASCADE
);
GO

-- 17. Tag
IF OBJECT_ID('dbo.Tag', 'U') IS NULL
CREATE TABLE dbo.Tag (
    TagID INT IDENTITY(1,1) PRIMARY KEY,
    TagName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- 18. VideoTag
IF OBJECT_ID('dbo.VideoTag', 'U') IS NULL
CREATE TABLE dbo.VideoTag (
    VideoID INT NOT NULL,
    TagID INT NOT NULL,
    PRIMARY KEY (VideoID, TagID),
    CONSTRAINT FK_VideoTag_Video FOREIGN KEY (VideoID) REFERENCES dbo.Video(VideoID) ON DELETE CASCADE,
    CONSTRAINT FK_VideoTag_Tag FOREIGN KEY (TagID) REFERENCES dbo.Tag(TagID) ON DELETE CASCADE
);
GO

-- 19. Watches
IF OBJECT_ID('dbo.Watches', 'U') IS NULL
CREATE TABLE dbo.Watches (
    StudentID INT NOT NULL,
    VideoID INT NOT NULL,
    LastWatchedDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    LastPosition INT NOT NULL DEFAULT 0 CHECK (LastPosition >= 0),
    PRIMARY KEY (StudentID, VideoID),
    CONSTRAINT FK_Watches_Student FOREIGN KEY (StudentID) REFERENCES dbo.Student(UserID),
    CONSTRAINT FK_Watches_Video FOREIGN KEY (VideoID) REFERENCES dbo.Video(VideoID)
);
GO

-- 20. Advertisement
IF OBJECT_ID('dbo.Advertisement', 'U') IS NULL
CREATE TABLE dbo.Advertisement (
    AdID INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    Description NVARCHAR(2000) NULL,
    Status NVARCHAR(20) NOT NULL CHECK (Status IN ('Active', 'Scheduled', 'Inactive', 'Expired')),
    AdminID INT NOT NULL,
    CourseID INT NULL,
    CONSTRAINT FK_Ad_Admin FOREIGN KEY (AdminID) REFERENCES dbo.Admin(UserID),
    CONSTRAINT FK_Ad_Course FOREIGN KEY (CourseID) REFERENCES dbo.Course(CourseID)
);
GO

-- 21. AdImage
IF OBJECT_ID('dbo.AdImage', 'U') IS NULL
CREATE TABLE dbo.AdImage (
    AdID INT NOT NULL,
    ImageNo INT NOT NULL,
    ImageURL NVARCHAR(500) NOT NULL,
    PRIMARY KEY (AdID, ImageNo),
    CONSTRAINT FK_AdImage_Ad FOREIGN KEY (AdID) REFERENCES dbo.Advertisement(AdID) ON DELETE CASCADE
);
GO

-- 22. AdSchedule
IF OBJECT_ID('dbo.AdSchedule', 'U') IS NULL
CREATE TABLE dbo.AdSchedule (
    AdID INT NOT NULL,
    ScheduleNo INT NOT NULL,
    StartDate DATETIME2 NOT NULL,
    EndDate DATETIME2 NOT NULL,
    PRIMARY KEY (AdID, ScheduleNo),
    CONSTRAINT CK_AdSchedule_Dates CHECK (EndDate >= StartDate),
    CONSTRAINT FK_AdSchedule_Ad FOREIGN KEY (AdID) REFERENCES dbo.Advertisement(AdID) ON DELETE CASCADE
);
GO

-- 23. AdView
IF OBJECT_ID('dbo.AdView', 'U') IS NULL
CREATE TABLE dbo.AdView (
    ViewID INT IDENTITY(1,1) PRIMARY KEY,
    ViewDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    AdID INT NOT NULL,
    UserID INT NULL,
    CONSTRAINT FK_AdView_Ad FOREIGN KEY (AdID) REFERENCES dbo.Advertisement(AdID),
    CONSTRAINT FK_AdView_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID)
);
GO

-- 24. TicketCategory
IF OBJECT_ID('dbo.TicketCategory', 'U') IS NULL
CREATE TABLE dbo.TicketCategory (
    CategoryID INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- 25. Ticket
IF OBJECT_ID('dbo.Ticket', 'U') IS NULL
CREATE TABLE dbo.Ticket (
    TicketID INT IDENTITY(1,1) PRIMARY KEY,
    Subject NVARCHAR(200) NOT NULL,
    Description NVARCHAR(2000) NOT NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT 'Open' CHECK (Status IN ('Open', 'In Progress', 'Resolved')),
    Priority NVARCHAR(20) NOT NULL DEFAULT 'Medium' CHECK (Priority IN ('Low', 'Medium', 'High')),
    CreatedDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ResolvedDate DATETIME2 NULL,
    CategoryID INT NOT NULL,
    StudentID INT NULL,
    TeacherID INT NULL,
    AssignedAdminID INT NULL,
    CONSTRAINT FK_Ticket_Category FOREIGN KEY (CategoryID) REFERENCES dbo.TicketCategory(CategoryID),
    CONSTRAINT FK_Ticket_Student FOREIGN KEY (StudentID) REFERENCES dbo.Student(UserID),
    CONSTRAINT FK_Ticket_Teacher FOREIGN KEY (TeacherID) REFERENCES dbo.Teacher(UserID),
    CONSTRAINT FK_Ticket_Admin FOREIGN KEY (AssignedAdminID) REFERENCES dbo.Admin(UserID),
    CONSTRAINT CK_Ticket_Author CHECK (
        (StudentID IS NULL AND TeacherID IS NOT NOT NULL) OR
        (StudentID IS NOT NULL AND TeacherID IS NULL)
    ),
    CONSTRAINT CK_Ticket_Resolved CHECK (
        (Status = 'Resolved' AND ResolvedDate IS NOT NULL) OR
        (Status <> 'Resolved' AND ResolvedDate IS NULL)
    ),
    CONSTRAINT CK_Ticket_Dates CHECK (ResolvedDate IS NULL OR ResolvedDate >= CreatedDate)
);
GO

-- 26. TicketResponse
IF OBJECT_ID('dbo.TicketResponse', 'U') IS NULL
CREATE TABLE dbo.TicketResponse (
    TicketID INT NOT NULL,
    ResponseNo INT NOT NULL,
    Message NVARCHAR(2000) NOT NULL,
    ResponseDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    AdminID INT NOT NULL,
    PRIMARY KEY (TicketID, ResponseNo),
    CONSTRAINT FK_Response_Ticket FOREIGN KEY (TicketID) REFERENCES dbo.Ticket(TicketID) ON DELETE CASCADE,
    CONSTRAINT FK_Response_Admin FOREIGN KEY (AdminID) REFERENCES dbo.Admin(UserID)
);
GO

-- 27. TicketAttachment
IF OBJECT_ID('dbo.TicketAttachment', 'U') IS NULL
CREATE TABLE dbo.TicketAttachment (
    TicketID INT NOT NULL,
    AttachmentNo INT NOT NULL,
    FileURL NVARCHAR(500) NOT NULL,
    PRIMARY KEY (TicketID, AttachmentNo),
    CONSTRAINT FK_Attachment_Ticket FOREIGN KEY (TicketID) REFERENCES dbo.Ticket(TicketID) ON DELETE CASCADE
);
GO

-- 28. Announcement
IF OBJECT_ID('dbo.Announcement', 'U') IS NULL
CREATE TABLE dbo.Announcement (
    AnnouncementID INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    Content NVARCHAR(3000) NOT NULL,
    PostedDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ExpiryDate DATETIME2 NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT 'Active' CHECK (Status IN ('Active', 'Expired', 'Cancelled')),
    TeacherID INT NULL,
    AdminID INT NULL,
    CourseID INT NULL,
    CONSTRAINT FK_Announcement_Teacher FOREIGN KEY (TeacherID) REFERENCES dbo.Teacher(UserID),
    CONSTRAINT FK_Announcement_Admin FOREIGN KEY (AdminID) REFERENCES dbo.Admin(UserID),
    CONSTRAINT FK_Announcement_Course FOREIGN KEY (CourseID) REFERENCES dbo.Course(CourseID),
    CONSTRAINT CK_Announcement_Author CHECK (
        (TeacherID IS NULL AND AdminID IS NOT NULL) OR
        (TeacherID IS NOT NULL AND AdminID IS NULL)
    ),
    CONSTRAINT CK_Announcement_Expiry CHECK (ExpiryDate IS NULL OR ExpiryDate >= PostedDate)
);
GO

-- 29. Event
IF OBJECT_ID('dbo.Event', 'U') IS NULL
CREATE TABLE dbo.Event (
    EventID INT IDENTITY(1,1) PRIMARY KEY,
    EventName NVARCHAR(200) NOT NULL,
    Description NVARCHAR(2000) NULL,
    EventDate DATE NOT NULL,
    StartTime TIME NOT NULL,
    Venue NVARCHAR(200) NOT NULL,
    Type NVARCHAR(30) NOT NULL CHECK (Type IN ('Webinar', 'Workshop', 'Deadline', 'Other')),
    Status NVARCHAR(20) NOT NULL DEFAULT 'Scheduled' CHECK (Status IN ('Scheduled', 'Cancelled', 'Completed')),
    TeacherID INT NULL,
    AdminID INT NULL,
    CourseID INT NULL,
    CONSTRAINT FK_Event_Teacher FOREIGN KEY (TeacherID) REFERENCES dbo.Teacher(UserID),
    CONSTRAINT FK_Event_Admin FOREIGN KEY (AdminID) REFERENCES dbo.Admin(UserID),
    CONSTRAINT FK_Event_Course FOREIGN KEY (CourseID) REFERENCES dbo.Course(CourseID),
    CONSTRAINT CK_Event_Author CHECK (
        (TeacherID IS NULL AND AdminID IS NOT NULL) OR
        (TeacherID IS NOT NULL AND AdminID IS NULL)
    )
);
GO

-- 30. RegistersFor
IF OBJECT_ID('dbo.RegistersFor', 'U') IS NULL
CREATE TABLE dbo.RegistersFor (
    StudentID INT NOT NULL,
    EventID INT NOT NULL,
    RegisteredDate DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (StudentID, EventID),
    CONSTRAINT FK_RegistersFor_Student FOREIGN KEY (StudentID) REFERENCES dbo.Student(UserID),
    CONSTRAINT FK_RegistersFor_Event FOREIGN KEY (EventID) REFERENCES dbo.Event(EventID) ON DELETE CASCADE
);
GO

-- 31. Notification
IF OBJECT_ID('dbo.Notification', 'U') IS NULL
CREATE TABLE dbo.Notification (
    NotificationID INT IDENTITY(1,1) PRIMARY KEY,
    Message NVARCHAR(1000) NOT NULL,
    Type NVARCHAR(30) NOT NULL CHECK (Type IN ('Quiz Result', 'Reply', 'Event Reminder', 'Announcement', 'System')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    IsRead BIT NOT NULL DEFAULT 0,
    UserID INT NOT NULL,
    CONSTRAINT FK_Notification_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

-- ====================================================================
-- Part E: Stored Procedure - Record Quiz Attempt with Validation
-- ====================================================================
IF OBJECT_ID('dbo.usp_RecordQuizAttempt', 'P') IS NOT NULL
    DROP PROCEDURE dbo.usp_RecordQuizAttempt;
GO

CREATE PROCEDURE dbo.usp_RecordQuizAttempt
    @StudentID INT,
    @QuizID INT,
    @Score DECIMAL(5,2)
AS
BEGIN
    SET NOCOUNT ON;

    -- 1. Validate Student existence
    IF NOT EXISTS (SELECT 1 FROM dbo.Student WHERE UserID = @StudentID)
    BEGIN
        RAISERROR('This student does not exist.', 16, 1);
        RETURN;
    END

    -- 2. Validate Quiz existence
    IF NOT EXISTS (SELECT 1 FROM dbo.Quiz WHERE QuizID = @QuizID)
    BEGIN
        RAISERROR('This quiz does not exist.', 16, 1);
        RETURN;
    END

    -- 3. Validate Score non-negativity
    IF @Score < 0
    BEGIN
        RAISERROR('Score cannot be negative.', 16, 1);
        RETURN;
    END

    -- 4. Save Attempt
    INSERT INTO dbo.QuizAttempt (StudentID, QuizID, Score, AttemptDate)
    VALUES (@StudentID, @QuizID, @Score, SYSDATETIME());

    -- 5. Return result status
    SELECT 
        @StudentID AS StudentID,
        @QuizID AS QuizID,
        @Score AS Score,
        q.PassMark,
        CASE WHEN @Score >= q.PassMark THEN 'Pass' ELSE 'Fail' END AS Result
    FROM dbo.Quiz q
    WHERE q.QuizID = @QuizID;
END
GO

-- ====================================================================
-- Part F: Trigger - Auto Notification on Quiz Attempt
-- ====================================================================
IF OBJECT_ID('dbo.trg_QuizAttempt_Notify', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_QuizAttempt_Notify;
GO

CREATE TRIGGER dbo.trg_QuizAttempt_Notify
ON dbo.QuizAttempt
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.Notification (Message, Type, CreatedAt, IsRead, UserID)
    SELECT 
        'You scored ' + CAST(i.Score AS NVARCHAR(10)) + '% in ' + q.Title + '.',
        'Quiz Result',
        SYSDATETIME(),
        0,
        i.StudentID
    FROM inserted i
    JOIN dbo.Quiz q ON i.QuizID = q.QuizID;
END
GO

-- ====================================================================
-- Part G: Sample Data Seed Script (DML Initial Data)
-- ====================================================================

-- 1. Insert Base Users (Admin, Teachers, Students)
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'admin@eduframe.lk')
BEGIN
    -- Admin (UserID = 1)
    INSERT INTO dbo.Users (FirstName, LastName, Email, PasswordHash, Role)
    VALUES ('System', 'Administrator', 'admin@eduframe.lk', '$2a$10$abcdefghijklmnopqrstuu', 'Admin');

    INSERT INTO dbo.Admin (UserID, Role)
    VALUES (1, 'Admin');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'kanishka@eduframe.lk')
BEGIN
    -- Teacher 1 (UserID = 2)
    INSERT INTO dbo.Users (FirstName, LastName, Email, PasswordHash, Role)
    VALUES ('Kanishka', 'Jayasinghe', 'kanishka@eduframe.lk', '$2a$10$abcdefghijklmnopqrstuu', 'Teacher');

    INSERT INTO dbo.Teacher (UserID, Role, Department, Bio)
    VALUES (2, 'Teacher', 'Computing', 'Professor in Software Architecture & Software Engineering');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'tharindu@eduframe.lk')
BEGIN
    -- Teacher 2 (UserID = 3)
    INSERT INTO dbo.Users (FirstName, LastName, Email, PasswordHash, Role)
    VALUES ('Tharindu', 'Senanayake', 'tharindu@eduframe.lk', '$2a$10$abcdefghijklmnopqrstuu', 'Teacher');

    INSERT INTO dbo.Teacher (UserID, Role, Department, Bio)
    VALUES (3, 'Teacher', 'Computing', 'Senior Lecturer specializing in Java and Object-Oriented Software Design');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'priyantha@eduframe.lk')
BEGIN
    -- Teacher 3 (UserID = 4)
    INSERT INTO dbo.Users (FirstName, LastName, Email, PasswordHash, Role)
    VALUES ('Priyantha', 'Alwis', 'priyantha@eduframe.lk', '$2a$10$abcdefghijklmnopqrstuu', 'Teacher');

    INSERT INTO dbo.Teacher (UserID, Role, Department, Bio)
    VALUES (4, 'Teacher', 'Engineering', 'Senior Lecturer specializing in Digital Electronics & Embedded Systems');
END

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'student1@eduframe.lk')
BEGIN
    -- Student 1 (UserID = 5)
    INSERT INTO dbo.Users (FirstName, LastName, Email, PasswordHash, Role)
    VALUES ('Ashendra', 'Adikari', 'student1@eduframe.lk', '$2a$10$abcdefghijklmnopqrstuu', 'Student');

    INSERT INTO dbo.Student (UserID, Role, EnrollmentYear)
    VALUES (5, 'Student', 2025);
END

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'student2@eduframe.lk')
BEGIN
    -- Student 2 (UserID = 6)
    INSERT INTO dbo.Users (FirstName, LastName, Email, PasswordHash, Role)
    VALUES ('Kasun', 'Perera', 'student2@eduframe.lk', '$2a$10$abcdefghijklmnopqrstuu', 'Student');

    INSERT INTO dbo.Student (UserID, Role, EnrollmentYear)
    VALUES (6, 'Student', 2025);
END
GO

-- 2. Insert Course Categories
IF NOT EXISTS (SELECT 1 FROM dbo.CourseCategory WHERE CategoryName = 'Computing')
    INSERT INTO dbo.CourseCategory (CategoryName) VALUES ('Computing');

IF NOT EXISTS (SELECT 1 FROM dbo.CourseCategory WHERE CategoryName = 'Engineering')
    INSERT INTO dbo.CourseCategory (CategoryName) VALUES ('Engineering');

IF NOT EXISTS (SELECT 1 FROM dbo.CourseCategory WHERE CategoryName = 'Business')
    INSERT INTO dbo.CourseCategory (CategoryName) VALUES ('Business');
GO

-- 3. Insert Sample Courses
IF NOT EXISTS (SELECT 1 FROM dbo.Course WHERE Title = 'Software Engineering & Architecture')
BEGIN
    DECLARE @CompID INT = (SELECT CategoryID FROM dbo.CourseCategory WHERE CategoryName = 'Computing');
    DECLARE @TeachKanishka INT = (SELECT UserID FROM dbo.Users WHERE Email = 'kanishka@eduframe.lk');

    INSERT INTO dbo.Course (Title, Description, CoverImage, CategoryID, TeacherID)
    VALUES (
        'Software Engineering & Architecture',
        'Comprehensive course covering SDLC methodologies, MVC architecture, design patterns, UML diagramming, and automated testing in modern enterprise systems.',
        '/images/thumb-mvc.jpg',
        @CompID,
        @TeachKanishka
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.Course WHERE Title = 'Object-Oriented Programming in Java')
BEGIN
    DECLARE @CompID2 INT = (SELECT CategoryID FROM dbo.CourseCategory WHERE CategoryName = 'Computing');
    DECLARE @TeachTharindu INT = (SELECT UserID FROM dbo.Users WHERE Email = 'tharindu@eduframe.lk');

    INSERT INTO dbo.Course (Title, Description, CoverImage, CategoryID, TeacherID)
    VALUES (
        'Object-Oriented Programming in Java',
        'Fundamental concepts of Object-Oriented Programming: classes, objects, encapsulation, inheritance, polymorphism, abstract classes, and interfaces.',
        '/images/thumb-oop.jpg',
        @CompID2,
        @TeachTharindu
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.Course WHERE Title = 'Digital Logic Design & Karnaugh Maps')
BEGIN
    DECLARE @EngID INT = (SELECT CategoryID FROM dbo.CourseCategory WHERE CategoryName = 'Engineering');
    DECLARE @TeachPriyantha INT = (SELECT UserID FROM dbo.Users WHERE Email = 'priyantha@eduframe.lk');

    INSERT INTO dbo.Course (Title, Description, CoverImage, CategoryID, TeacherID)
    VALUES (
        'Digital Logic Design & Karnaugh Maps',
        'Combinational logic design, Boolean algebra simplification, K-Map minimization techniques, gates, multiplexers, and sequential logic circuits.',
        '/images/thumb-kmaps.jpg',
        @EngID,
        @TeachPriyantha
    );
END
GO

-- 4. Insert Sample Modules
IF NOT EXISTS (SELECT 1 FROM dbo.Module WHERE Title LIKE 'Module 1: Introduction to MVC Architecture%')
BEGIN
    DECLARE @Course1ID INT = (SELECT CourseID FROM dbo.Course WHERE Title = 'Software Engineering & Architecture');
    
    INSERT INTO dbo.Module (Title, OrderIndex, CourseID) VALUES ('Module 1: Introduction to MVC Architecture & Design Patterns', 1, @Course1ID);
    INSERT INTO dbo.Module (Title, OrderIndex, CourseID) VALUES ('Module 2: Creational Patterns - Singleton & Factory', 2, @Course1ID);
END
GO

-- 5. Insert Sample Announcements & Events
IF NOT EXISTS (SELECT 1 FROM dbo.Announcement WHERE Title = 'Welcome to Semester 1 Academic Year 2026')
BEGIN
    DECLARE @AdminID INT = (SELECT UserID FROM dbo.Users WHERE Email = 'admin@eduframe.lk');
    DECLARE @Course1ID INT = (SELECT CourseID FROM dbo.Course WHERE Title = 'Software Engineering & Architecture');

    INSERT INTO dbo.Announcement (Title, Content, PostedDate, Status, AdminID, CourseID)
    VALUES (
        'Welcome to Semester 1 Academic Year 2026',
        'All course materials, syllabus documents, and assignment schedules have been published across the EduFrame portal.',
        SYSDATETIME(),
        'Active',
        @AdminID,
        @Course1ID
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.Event WHERE EventName = 'Software Architecture & Design Review')
BEGIN
    DECLARE @TeachKanishka INT = (SELECT UserID FROM dbo.Users WHERE Email = 'kanishka@eduframe.lk');
    DECLARE @Course1ID INT = (SELECT CourseID FROM dbo.Course WHERE Title = 'Software Engineering & Architecture');

    INSERT INTO dbo.Event (EventName, Description, EventDate, StartTime, Venue, Type, Status, TeacherID, CourseID)
    VALUES (
        'Software Architecture & Design Review',
        'Interactive live session reviewing MVC architecture patterns, UML component diagrams, and Spring Boot service structure for SE2030 assignment.',
        '2026-10-14',
        '14:30:00',
        'https://meet.google.com/eduframe-se2030',
        'Webinar',
        'Scheduled',
        @TeachKanishka,
        @Course1ID
    );
END
GO

