# AIBIZ (English README)
## Project Introduction
AIBIZ is an AI-driven low-code business platform built on **RuoYi-Vue3 (SpringBoot4 + Jakarta + JDK17+)**, natively integrated with the Flowable workflow engine. It encapsulates common business modules including lightweight OA, basic ERP, HR management and DRP distribution system.

### Core Differentiated Features (Open-Source Community Highlight)
1. **Conversation-First Homepage, Reimagined Admin UI**
Left sidebar is hidden by default, presenting a clean blank dialogue workspace with a persistent command input box at the bottom. Click the hamburger menu at the top-left corner to expand the full sidebar for system configuration, permission management and workflow editing.
2. **Natural Language Driven Operation, Zero-Click Access to Business Pages**
Simply input text commands to instantly load data tables or pop creation/edit forms. Built-in local instruction parser works out of the box without complex configuration.
3. **Native Flowable 8 Workflow Orchestration Engine**
Visual drag-and-drop BPMN canvas for business logic design. Service tasks support invoking custom Java methods to fetch return values, pass data through process variables and execute conditional branch judgments, covering daily approval and no-code logic arrangement.
4. **Lightweight Private Large Model Compatibility**
Reserved unified adapter interface for connecting self-hosted LLMs, supporting intelligent semantic recognition and custom command expansion.
5. **Containerized One-Click Deployment**
Complete Docker images and initialized SQL scripts are provided, enabling quick standalone deployment without complicated environment setup.

> Note: This repository is the **open-source standalone community edition** with single-tenant architecture, suitable for individual developers and small & micro enterprises. Enterprise-grade capabilities such as multi-tenant SaaS, microservice cluster, third-party deep connectors and compliance audit logs are delivered as closed-source commercial extension packages.

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
- Matching Frontend Repository: https://gitee.com/ys-gitee/RuoYi-Vue3
- Custom Modules: AI dialogue panel, command parsing component, BPMN workflow designer

### Layered Architecture
1. Base Layer: RuoYi native permission, dictionary, parameter, file management & code generator (fully open-source)
2. AI Interaction Layer: Dialogue homepage, command parser, LLM adapter (core open-source highlight)
3. Business Module Layer: Lightweight OA, HR, basic ERP & DRP single-tenant modules (open-source)
4. Workflow Layer: Fundamental BPMN canvas, service tasks, variable transmission, conditional gateways (open-source)
5. Enterprise Extension Layer: Multi-tenant isolation, microservice, domestic database adaptation, third-party connectors (closed-source commercial Jar dependencies)

## Installation Guide
### Environment Prerequisites
1. JDK ≥ 17
2. MySQL ≥ 8.0
3. Redis ≥ 6.2
4. Node.js ≥ 18

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
3. Configuration Modification
Update database & Redis connection in `application-dev.yml`
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
3. Start full stack (MySQL + Redis + Application) via docker-compose

## Usage Instructions
1. Quick Start with AI Dialogue
Directly input commands in the bottom input box after entering homepage, examples:
- User List: Pop up employee data table
- New Department: Quick create department popup
- My Todo Approvals: Show pending Flowable workflow tasks
2. System Configuration Entrance
Click top-left hamburger icon to expand full sidebar for permission, role, workflow canvas and parameter configuration
3. Workflow Orchestration
Enter "Workflow Design" menu, drag BPMN nodes to build business logic, support Bean method invocation, variable passing and conditional branching
4. Private LLM Integration
Fill LLM API address & secret in System Config - AI Settings to enable advanced semantic parsing

### Open-Source vs Commercial Function Boundary
#### ✅ Free Open-Source Community Edition
- Full AI dialogue interaction, single-tenant permission system
- Basic Flowable workflow orchestration & approval functions
- Standalone OA/HR/basic ERP/DRP business modules
- Docker standalone deployment, complete API docs & source code
- Free for personal and small commercial use

#### ⚠️ Paid Closed-Source Commercial Extensions (Independent Jar Packages)
- Multi-tenant SaaS data isolation, table/database sharding
- Microservice cluster, distributed transaction, Nacos registry
- DingTalk/WeCom/LDAP SSO, deep ERP integration connectors
- Workflow monitoring dashboard, operation audit log, data watermark & desensitization
- Domestic database adaptation (Dameng, Kingbase), domestic server compatibility
- Dedicated technical support, custom development & annual upgrade service

## Contribution Guide
1. Fork this repository
2. Create new branch: `Feat_xxx` for new features / `Fix_xxx` for bug fixes
3. Complete local coding and self-testing
4. Submit standardized commit messages
5. Create Pull Request for review and merge

## Repository Sync Note
This Gitee repo acts as the primary source, auto-synced to GitHub. All commits should only be pushed to Gitee. DO NOT commit code directly on GitHub to avoid sync conflicts.

## Open Source License
The community edition is licensed under **Apache 2.0**. Free learning and commercial use are allowed; retain the original copyright notice for secondary distribution. Commercial extension modules require official authorization for commercial deployment.

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
4. Communication Channel
Submit bugs & feature requests via Issues; contact the author for commercial licensing and custom development cooperation.