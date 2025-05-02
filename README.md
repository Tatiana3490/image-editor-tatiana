# 🖼️ Image Editor - Filtros Multihilo en JavaFX

Aplicación JavaFX que permite aplicar filtros de imagen utilizando procesamiento concurrente. Desarrollada como parte de la Actividad de Aprendizaje en el módulo de **Programación de Servicios y Procesos**.

---

## 🚀 Funcionalidades principales

* ✅ **Aplicación de filtros básicos:**

  * Escala de grises
  * Inversión de color
  * Aumento de brillo

* ✅ **Visualización previa y posterior de la imagen**

* ✅ **Procesamiento de imágenes por lotes (carpetas)**

* ✅ **Ejecución concurrente con control de número de hilos**

* ✅ **Historial de operaciones persistente**

* ✅ **Barra de progreso y ventanas emergentes informativas**

* ✅ **Deshacer cambios (volver al estado original)**

* ✅ **Splash screen al iniciar la app**

* ✅ **Botón para abrir carpeta de imágenes procesadas**

* ✅ **Botón para borrar historial**

---

## 🧵 Tecnologías utilizadas

* Java 20
* JavaFX (Controls, FXML, Swing)
* Threads, ExecutorService
* Diseño MVC básico
* Git + GitHub

---

## 📁 Estructura del proyecto

```
src/
├── controller/       # Controladores JavaFX (FilterController)
├── filters/          # Filtros de imagen (Brightness, Grayscale, Inversion)
├── task/             # Lógica concurrente (FilterTask, ImageFilter)
├── MainApp.java      # Clase principal
resources/
├── main_layout.fxml  # Interfaz JavaFX
├── splash.fxml       # Splash Screen
history.txt           # Historial de filtros aplicados
```

---

## 🛠️ Instrucciones de ejecución

1. Instala Java 20 y JavaFX
2. Asegúrate de tener los módulos JavaFX configurados (`javafx.controls`, `javafx.fxml`, `javafx.swing`)
3. Ejecuta la clase `MainApp`
4. O si usas Maven/IDEA, simplemente pulsa el botón de "Run"

---

## 🧐 Autor

**Tatiana** – Alumna que ha sobrevivido a Java multihilo.
Proyecto realizado para el profesor con más filtros que Photoshop.

---

## 🐙 Repositorio

> Este proyecto está gestionado con GitHub utilizando Issues, ramas y commits relacionados.
