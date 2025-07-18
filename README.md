# WhatsApp Social Network – Spring MVC Project

## Students

- Ron Avital - ronav@edu.jmc.ac.il
- David Weschler - davidwes@edu.jmc.ac.il

## Overview

This project implements a full-stack **WhatsApp-like social network** using **Spring Boot MVC**, **JPA (MySQL)**, **Spring Security**, and **Thymeleaf**. It allows users to chat via private, group, or community chatrooms, send messages and files, report inappropriate content, and for administrators to moderate users and broadcast announcements.

Watch the full demo on [YouTube](https://youtu.be/3yQcJbPibIs)

---

## Table of Contents

- [Screenshots](#screenshots)
- [How to Run the Project](#how-to-run-the-project)
  - [1. MySQL DB Setup with XAMPP (for Windows)](#1-mysql-db-setup-with-xampp-for-windows)
  - [2. Increase SQL Packet Size Limit](#2-increase-sql-packet-size-limit)
  - [3. Initialize the SQL Database](#3-initialize-the-sql-database)
- [Admin Accounts](#admin-accounts)
- [Feature Overview](#feature-overview)
  - [General Features (All Users)](#general-features-all-users)
  - [Admin-only Features](#admin-only-features)
- [Database Tables Diagram](#database-tables-diagram)
- [Technical Highlights](#technical-highlights)

---

## Screenshots

| Login Page                                              | Home Page                                                 | Messaging Interface                                      |
| ------------------------------------------------------- | --------------------------------------------------------- | -------------------------------------------------------- |
| ![](src/main/resources/static/screenshots/login.png)    | ![](src/main/resources/static/screenshots/home.png)       | ![](src/main/resources/static/screenshots/myChats.png)   |
| Community Browser                                       | Chatroom Management                                       | Message Report Page                                      |
| ![](src/main/resources/static/screenshots/discover.png) | ![](src/main/resources/static/screenshots/manage.png)     | ![](src/main/resources/static/screenshots/report.png)    |
| File Upload Flow                                        | Admin Report Panel                                        | Broadcast Creation Page                                  |
| ![](src/main/resources/static/screenshots/file.png)     | ![](src/main/resources/static/screenshots/adminPanel.png) | ![](src/main/resources/static/screenshots/broadcast.png) |
| Banned Page                                             |                                                           |                                                          |
| ![](src/main/resources/static/screenshots/banned.png)   |                                                           |                                                          |

---

## How to Run the Project

### 1. MySQL DB Setup with XAMPP (for Windows)

#### Installing XAMPP (Recommended)

1. Download XAMPP from [https://www.apachefriends.org/index.html](https://www.apachefriends.org/index.html)
2. During installation, **DESELECT TOMCAT** from the components list (it installs an outdated version that may conflict).
3. Complete installation and run XAMPP Control Panel.

#### Starting XAMPP

- Open the XAMPP Control Panel
- Click **Start** on both:
  - **Apache**
  - **MySQL**

The MySQL database server will now be accessible on `localhost:3306`.

### 2. Increase SQL Packet Size Limit

- Open [phpMyAdmin](http://localhost/phpmyadmin)
- Go to `SQL` tab
- run this: `SET GLOBAL max_allowed_packet=67108864;`

### 3. Initialize the SQL Database

- Open [phpMyAdmin](http://localhost/phpmyadmin)
- Create a new database named `ex4`
- (OPTIONAL: If you already have a ready database): Import your SQL file using the **Import** tab in phpMyAdmin

---

## Admin Accounts

Two admin users are included in the database via the `ex4.sql`. Initially, they are `Admin` users already (if imported our given file).

**Admin usernames:**

- `webmasterone2025@gmail.com`
- `webmasterthree2025@gmail.com`

**Password** `Web2025$`

`NOTE`: If starting from scratch: start the program -> Login with the user you want to make an admin -> go to [phpMyAdmin](http://localhost/phpmyadmin) and go to `user` table -> manually change `Role` cloumn from `USER` to `ADMIN` -> re-run the program -> Your user is now an admin!

## Feature Overview

### General Features (All Users)

- Google OAuth2 login
- Create and join:
  - Private chatrooms
  - Group chatrooms (editable name, member add/remove)
  - Community chatrooms (publicly discoverable)
- Real-time styled messaging interface
- File attachments per message
- Message reporting system (one report per user per message)
- Broadcast viewer for admin announcements

### Admin-only Features

- View and moderate reported messages
- Dismiss or act on reports (24h, 1w, permanent bans)
- View banned users with expiration logic
- Create, edit, and delete time-limited broadcast messages
- Restricted from messaging/chatroom access
- Polling-enabled admin dashboard for new reports

---

## Database Tables Diagram

## ![Database ER Diagram](db-diagram-ex4.png)

---

## Technical Highlights

- **Session Usage:**

  - Tracks login state
  - Stores recent chatrooms visited
  - Controls visibility and access to session-specific content

- **Authentication:**

  - Uses Spring Security with Google OAuth2
  - Role-based access control enforced in controllers and views

- **WebSocket:**

  - Used for **real-time messaging** between users inside chatrooms
  - Enables live updates without polling or refreshing the page

- **Polling:**
  - Used in:
    - Admin dashboard to check for new reports
    - Home page to update and expire broadcast messages in real time
