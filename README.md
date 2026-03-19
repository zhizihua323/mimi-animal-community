# 🐾 Mimi Animal Community (咪咪动物社区)

[![Gitee](https://img.shields.io/badge/Gitee-zhizihua323-red.svg)](https://gitee.com/zhizihua323/mimi-animal-community)
[![GitHub](https://img.shields.io/badge/GitHub-zhizihua323-black.svg)](https://github.com/zhizihua323/mimi-animal-community)

---

### 🌟 项目简介
**Mimi Animal Community** 是一个基于 **Spring Cloud / Spring Boot** 架构的流浪动物救助与社交平台。项目深度集成 **Docker** 容器化部署方案，并结合 **Nacos、RabbitMQ、MinIO** 等核心组件，旨在打造一个高性能、可扩展的现代化宠物社区系统。

---

### 🛠️ 全栈技术架构

#### **后端与中间件 (Infrastructure & Middleware)**
| 组件 | 应用场景 |
| :--- | :--- |
| **Docker** | 容器化部署，通过 `docker-compose` 实现一键环境搭建 |
| **Nacos** | 服务注册中心与分布式配置中心，实现服务动态发现与配置热更新 |
| **RabbitMQ** | 消息中间件，负责异步解耦（如点赞通知、站内信推送等） |
| **MinIO** | 私有化对象存储，高效管理图片、视频等非结构化数据 |
| **Redis** | 分布式缓存，提升热点数据访问性能并支持分布式锁 |
| **MySQL 8.0** | 核心业务关系型数据库 |
| **MyBatis-Plus** | 简化持久层开发，提升代码产出效率 |

#### **前端与跨端 (Cross-platform)**
* **Vue 3 + UniApp**：采用组件化开发模式，支持一套代码多端（H5/小程序/App）分发。

---

### 📂 项目目录结构
```text
mimiday
├── MIMI_Front         # 前端项目 (UniApp/Vue3)
├── MIMILIFE           # 后端项目 (Spring Boot/Cloud)
│   ├── mimi-model     # 基础实体类与 DTO 模块
│   └── ...            # 核心业务逻辑实现
├── docker-compose.yml # Docker 一键编排文件
└── README.md          # 项目说明文档