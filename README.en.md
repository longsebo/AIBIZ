# AIBIZ Open-Source Middle Platform
## Project Introduction
AIBIZ is an AI-driven low-code business middle platform newly developed based on **RuoYi-Vue3 (SpringBoot4 + Jakarta + JDK17+)**, with built-in Flowable workflow engine. Relying on the zero-code application building kernel, users can quickly build various business systems such as OA, ERP, HR, and DRP distribution via visual configuration.

### Core Differentiated Highlights (Open-Source Community Advantages)
1. **Conversation-First Homepage, Revolutionary Admin Interaction**
The left sidebar is hidden by default, presenting a lightweight blank dialogue workspace with a persistent AI command input box at the bottom. Click the hamburger menu at the top-left corner to expand the full sidebar for system configuration, permission maintenance and workflow designer editing.
2. **Natural Language Driven Operations, Zero-Click Access to Business Pages**
Input text commands to instantly load data tables or pop creation/edit forms. The built-in front-end local instruction parser works out of the box without complex configuration.
3. **Native Integrated Flowable Workflow Orchestration Engine**
Visual drag-and-drop BPMN canvas for business logic design. Service tasks support invoking custom Java methods to obtain return values, transmit data through process variables and execute conditional branch judgments, covering daily approval and no-code logic arrangement.
4. **Lightweight Private LLM Compatibility**
Unified adapter interfaces are reserved for connecting self-hosted large language models to support semantic recognition and custom instruction expansion.
5. **One-Click Containerized Deployment**
Complete Docker images and initialized SQL scripts are provided for quick standalone deployment without complicated environment setup.

> Note: This repository is the **open-source standalone community edition** with single-tenant architecture and built-in lightweight free business templates. Individuals and small & micro enterprises may freely download and self-configure business systems. Enterprise-grade capabilities including multi-tenant SaaS, microservice clusters, third-party deep connectors and compliance audit logs are delivered as closed-source commercial extension JAR packages.
> Business Model Description: The entire underlying platform engine is fully open-source and free. Revenue comes from selling vertical industry packaged application templates (commercial DRP, manufacturing ERP, group HR, etc.). Paid templates are independent commercial assets that require authorization verification during import; unauthorized distribution and resale are prohibited.

## Software Architecture
### Tech Stack
#### Backend
- Core Framework: SpringBoot 4.x, Spring 7, Jakarta Servlet
- Persistence Layer: MyBatis-Plus, MySQL 8.0
- Workflow Engine: Flowable 8.0
- Middleware: Redis, Quartz Scheduled Task, Hutool Toolkit, Swagger API Docs
- Runtime Environment: JDK 17 / JDK 21

#### Frontend
- Tech Stack: Vue3 + Vite + Element Plus
- Base Framework Repository: https://gitee.com/ys-gitee/RuoYi-Vue3
- Self-developed Modules: AI dialogue panel, command parsing component, BPMN workflow designer, zero-code application designer, template import authorization verification module

### Layered Architecture
1. **Base Layer**: Native RuoYi permission, dictionary, parameter, file management & code generator (fully open-source)
2. **AI Interaction Layer**: Dialogue homepage, command parser, LLM adapter (core open-source highlight)
3. **Zero-Code Builder Layer**: Data modeling, form designer, list view config, app template import/export, full open-source authorization verification logic
4. **Basic Business Layer**: Lightweight free templates for OA, HR, basic ERP and DRP (built-in open-source)
5. **Workflow Layer**: Fundamental BPMN canvas, service tasks, variable transmission, conditional gateways (open-source)
6. **Enterprise Extension Layer**: Multi-tenancy, microservice, domestic database adaptation, third-party connectors (closed-source commercial JAR dependencies)

## Installation Guide
### Environment Prerequisites
- JDK ≥ 17
- MySQL ≥ 8.0
- Redis ≥ 6.2
- Node.js ≥ 18

### Local Deployment Steps
1. Clone Repository
```bash
# Domestic Gitee Repository (Recommended)
git clone https://gitee.com/longsebo/aibiz.git
# Synchronized GitHub Mirror
git clone https://github.com/longsebo/aibiz.git
```
2. Database Initialization
Execute `sql/aibiz_community.sql` to create database and import baseline data
3. Modify Configuration
Update database & Redis connection parameters in `application-dev.yml`
4. Launch Backend
Run main class `AibizApplication.java`, default port: 8080
5. Launch Frontend
Enter frontend directory, install dependencies and start dev server
```bash
npm install
npm run dev
```
6. Access System
Visit http://localhost:80 in browser
Default account: admin | Default password: admin123

### One-Click Docker Deployment
1. Build project package
```bash
mvn clean package -DskipTests
```
2. Build Docker image with built-in Dockerfile
3. Start full stack services (MySQL + Redis + Application) via docker-compose

## Usage Instructions
### Quick Start with AI Dialogue
Directly input commands in the bottom input box after entering homepage, examples:
- User List: Pop up employee data table
- New Department: Quick create department popup
- My Todo Approvals: Show pending Flowable workflow tasks

### System Configuration Entrance
Click the top-left hamburger icon to expand the full sidebar for permission, role, workflow canvas, parameter configuration and application template management.

### Workflow Orchestration
Enter "Workflow Design" menu, drag BPMN nodes to build business logic, support Bean method invocation, variable passing and conditional branching.

### Private LLM Integration
Fill LLM API address & secret in System Config - AI Settings to enable advanced semantic parsing.

### Application Template Usage Rules
1. Built-in lightweight free templates: No authorization verification, one-click import, free modification for internal use.
2. Paid industry business templates: Delivered as independent ZIP packages with offline authorization certificates. Automatic enterprise license verification during import, only available for internal use of purchasing enterprises. Resale and secondary distribution are forbidden.

## Open-Source vs Commercial Function Boundary
### ✅ Free Open-Source Community Edition
- Full AI dialogue interaction, single-tenant permission system
- Complete zero-code visual builder engine, fully open-source template import/export & verification code
- Basic Flowable workflow orchestration & approval functions
- Lightweight free business templates for OA/HR/basic ERP/DRP
- Docker standalone deployment, complete API documents, full unencrypted underlying source code
- Free commercial use for individuals and small enterprises, unlimited self-built business workflows

### ⚠️ Paid Commercial Assets (Two Separate Paid Products)
1. Vertical Industry Application Template ZIP Packages
    - Complete business solutions: Chain DRP Distribution, Manufacturing ERP, Group HR Performance, Retail Integration
    - Deliverables: Data models, forms, approval workflows, dashboards, print templates, business initialization SQL, exclusive enterprise authorization file
    - Benefits: Permanent single-enterprise license, full deployment docs, annual template updates & remote troubleshooting
2. Closed-Source Enterprise Extension JAR Packages
    - Multi-tenant SaaS data isolation, database & table sharding
    - Microservice cluster, distributed transaction, Nacos registry
    - DingTalk/WeCom/LDAP SSO, deep financial system connectors
    - Workflow monitoring dashboard, operation audit log, data watermark & desensitization
    - Domestic database compatibility (Dameng, Kingbase), domestic server adaptation
    - Dedicated technical support, custom development & annual upgrade service

## Contribution Guide
1. Fork this repository
2. Create new branch: `Feat_xxx` for new features / `Fix_xxx` for bug fixes
3. Complete local coding and self-testing
4. Submit standardized commit messages
5. Create Pull Request for review and merge

## Repository Sync Note
This Gitee repo acts as the primary source, auto-synced to GitHub. All commits should only be pushed to Gitee. DO NOT commit code directly on GitHub to avoid sync conflicts.

## Open Source License & Copyright Statement
1. The community platform engine is licensed under **Apache 2.0**. Free learning and commercial use are allowed; retain the full original copyright notice for secondary distribution.
2. All underlying template import and verification source code is fully open-source without encryption or closed modules.
3. Paid industry templates and enterprise extension JARs are independent copyrighted commercial assets protected by software copyright registration. Unauthorized distribution, resale and reverse engineering are prohibited.
4. The project has obtained official software copyright registration. Legal action will be taken against any commercial plagiarism or pirated distribution.

## Extra Tips
1. Multi-language README support: `Readme_zh.md` (Chinese), `Readme_en.md` (English)
2. Official Gitee Resources
    - Gitee Official Blog: blog.gitee.com
    - Outstanding Open-Source Projects: https://gitee.com/explore
    - GVP (Gitee Valuable Project) Campaign
    - Gitee Official Docs: https://gitee.com/help
    - Gitee Star Creator Program: https://gitee.com/gitee-stars/
3. Repository Supporting Assets
    - One-click Docker deployment scripts
    - Pre-built Flowable BPMN workflow templates
    - Sample config files for private LLM connection
    - Complete development, deployment & demo documents

## Contact & Business Inquiries
Author ID: lxb20110121
For commercial consultation, template purchase, custom development and enterprise extension licensing
Business Email: 7432731@qq.com

### Communication Channels
1. Repository Issues: Submit bug reports and feature requests for open-source community issues
2. Email Communication: Business cooperation, paid templates, privatized deployment and enterprise custom development matters