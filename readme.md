# Aaspass

Aaspass is a location-based event discovery platform. It helps users create events, find nearby events, and receive updates about activities happening around them.

## Overview

The project is built as a backend service using Spring Boot. It provides APIs for user authentication, event management, location-based discovery, image uploads, and notifications.

## What This Project Uses

- **Spring Boot**: Used to build the backend APIs and manage the application flow.
- **PostgreSQL**: Used as the main database for storing users, events, event details, and upload records.
- **Redis**: Used for fast location-based searches, such as finding nearby users or events.
- **Google OAuth**: Used for secure user login without managing passwords directly.
- **AWS S3**: Used to store event images outside the main database.
- **Presigned URLs**: Used so users can upload images directly to S3 in a controlled and secure way.
- **Email Service**: Used for sending notifications or communication to users.
- **Spring Security**: Used to protect APIs and manage authenticated access.

## Main Functionality

- Users can log in using Google.
- Users can create and manage events.
- Events can include images, descriptions, tags, location, and seat information.
- Users can discover nearby events based on location.
- The system can track nearby users for event notifications.
- Event images are uploaded securely through S3.

## Purpose

The goal of Aaspass is to make local event discovery simple by combining event management, user location, image uploads, and notifications in one backend system.
