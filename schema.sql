-- ====================================================================
-- EduFrame Database DDL Script for Microsoft SQL Server (MSSQL)
-- Database Name: [EduFrame-db]
-- Author: EduFrame Team (SE2030 Group B4G1-06)
-- ====================================================================

-- 1. Create Database if it does not exist
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = N'EduFrame-db')
BEGIN
    CREATE DATABASE [EduFrame-db];
END
GO

USE [EduFrame-db];
GO

-- 2. Create Users Table
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL,
        username VARCHAR(100) NOT NULL,
        email VARCHAR(150) NOT NULL,
        password VARCHAR(255) NOT NULL,
        full_name VARCHAR(200) NOT NULL,
        role VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
        created_at DATETIME2(7) NOT NULL,
        updated_at DATETIME2(7) NOT NULL,
        CONSTRAINT PK_users PRIMARY KEY (id),
        CONSTRAINT UQ_users_username UNIQUE (username),
        CONSTRAINT UQ_users_email UNIQUE (email)
    );
END
GO

-- 3. Create Announcements & Events Table
IF OBJECT_ID(N'dbo.announcements', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.announcements (
        id BIGINT IDENTITY(1,1) NOT NULL,
        type VARCHAR(20) NOT NULL, -- 'ANNOUNCEMENT' or 'LIVE_EVENT'
        title VARCHAR(250) NOT NULL,
        content VARCHAR(3000) NOT NULL,
        author_id VARCHAR(100) NOT NULL,
        course_id VARCHAR(50) NOT NULL,
        event_date DATE NULL,
        start_time TIME(7) NULL,
        end_time TIME(7) NULL,
        location_url VARCHAR(500) NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'CANCELLED', 'EXPIRED'
        created_at DATETIME2(7) NOT NULL,
        updated_at DATETIME2(7) NOT NULL,
        CONSTRAINT PK_announcements PRIMARY KEY (id)
    );
END
GO

-- 4. Create Support Tickets Table (UC-05 Subsystem)
IF OBJECT_ID(N'dbo.tickets', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tickets (
        id BIGINT IDENTITY(1,1) NOT NULL,
        ticket_id VARCHAR(30) NOT NULL,
        subject VARCHAR(200) NOT NULL,
        description VARCHAR(2000) NOT NULL,
        attachment_name VARCHAR(255) NULL,
        attachment_type VARCHAR(100) NULL,
        status VARCHAR(30) NOT NULL DEFAULT 'Open',
        created_at DATETIME2(7) NOT NULL,
        updated_at DATETIME2(7) NOT NULL,
        CONSTRAINT PK_tickets PRIMARY KEY (id),
        CONSTRAINT UQ_tickets_ticket_id UNIQUE (ticket_id)
    );
END
GO

-- 5. Seed Initial Default Users (Password: password123 encoded with BCrypt)
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = 'admin')
BEGIN
    INSERT INTO dbo.users (username, email, password, full_name, role, created_at, updated_at)
    VALUES (
        'admin', 
        'admin@sliit.lk', 
        '$2a$10$e8W/yOshbHwF./sWpE2bfeQz8/uM/XgJ1p/c.zM4s8zW8F0LwE.8G', -- BCrypt hash of 'password123'
        'System Administrator', 
        'ADMIN', 
        GETDATE(), 
        GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = 'teacher')
BEGIN
    INSERT INTO dbo.users (username, email, password, full_name, role, created_at, updated_at)
    VALUES (
        'teacher', 
        'kanishka.j@sliit.lk', 
        '$2a$10$e8W/yOshbHwF./sWpE2bfeQz8/uM/XgJ1p/c.zM4s8zW8F0LwE.8G', 
        'Prof. Kanishka Jayasinghe', 
        'TEACHER', 
        GETDATE(), 
        GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = 'student')
BEGIN
    INSERT INTO dbo.users (username, email, password, full_name, role, created_at, updated_at)
    VALUES (
        'student', 
        'student@my.sliit.lk', 
        '$2a$10$e8W/yOshbHwF./sWpE2bfeQz8/uM/XgJ1p/c.zM4s8zW8F0LwE.8G', 
        'Kasun Perera', 
        'STUDENT', 
        GETDATE(), 
        GETDATE()
    );
END
GO
