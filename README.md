# KingCalculator 🧮👑

> **Advanced Algebraic Calculator & AI Tutor for University Students**  
> *Calculadora algebraica avanzada y tutor IA para estudiantes universitarios.*

---

## 🌐 Tabla de Contenidos / Table of Contents
- [Español](#-español)
  - [Acerca del Proyecto](#acerca-del-proyecto)
  - [Características Principales](#características-principales)
  - [Arquitectura y Tecnologías](#arquitectura-y-tecnologías)
  - [Estructura del Proyecto](#estructura-del-proyecto)
  - [Instalación y Configuración](#instalación-y-configuración)
- [English](#-english)
  - [About the Project](#about-the-project)
  - [Key Features](#key-features)
  - [Tech Stack](#tech-stack)
  - [Getting Started](#getting-started)
- [Autor / Author](#-autor--author)

---

## 🇪🇸 Español

### Acerca del Proyecto
**KingCalculator** es una aplicación móvil diseñada específicamente para cubrir las necesidades de cálculo algebraico y matemático de estudiantes de educación superior e ingeniería. Ofrece una interfaz estilizada, limpia y minimalista, optimizada para evitar distracciones durante la resolución de problemas complejos. Además, integra la potencia de la API de **Google Gemini** para actuar como un tutor inteligente que explica el paso a paso de cada ejercicio.

### Características Principales
- 📐 **Motor Algebraico Avanzado:** Evaluación y resolución de expresiones algebraicas complejas, ecuaciones, radicales y operaciones fundamentales.
- 🤖 **Tutor de IA con Google Gemini:** Análisis y explicación guiada paso a paso de problemas matemáticos complejos.
- 🎨 **Interfaz Minimalista y Sólida:** Diseño con paleta oscura en azul profundo, sin distracciones visuales.
- ⚡ **Desarrollo Nativo:** Rendimiento fluido creado para Android con Kotlin y Gradle.

### Arquitectura y Tecnologías
- **Lenguaje:** Kotlin
- **Sistema de Construcción:** Gradle (`build.gradle.kts`)
- **Integración de IA:** Google Gemini API
- **Arquitectura:** MVVM / Clean Architecture

### Estructura del Proyecto
```text
KingCalculator/
├── app/                  # Código fuente de la aplicación Android
├── gradle/               # Configuraciones del wrapper de Gradle
├── .env.example          # Plantilla de variables de entorno (API Keys)
├── build.gradle.kts      # Configuración global del proyecto
└── settings.gradle.kts   # Configuración de módulos
