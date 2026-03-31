# 🎨 Real-Time Neural Style Transfer in Java

> Apply the painting styles of Van Gogh, Picasso, Monet, and more — **live on your webcam** — using Java + OpenCV DNN.

---

## 📸 Demo

```
[Original Frame]  ──►  [Van Gogh Style]  ──►  [Candy Style]
    (Webcam)              (Swirling blues)      (Vivid colors)
```

Switch between styles instantly with a button click or number key.

---

## 🧠 How It Works

This project uses **Fast Neural Style Transfer** — a technique where a neural network is trained to transform images into a specific artistic style in a single forward pass (no iterative optimization needed at runtime).

At runtime:
1. Webcam frames are captured with **OpenCV**
2. Each frame is pre-processed into a blob
3. A pre-trained **Torch (.t7)** model runs via **OpenCV's DNN module**
4. The stylized output is displayed in real time using **Java Swing**

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language  | Java 11+ |
| Computer Vision | OpenCV 4.9 (via JavaCV / ByteDeco) |
| Neural Network Inference | OpenCV DNN module |
| Style Models | Fast Neural Style Transfer (.t7 Torch format) |
| UI | Java Swing |
| Build | Apache Maven |

---

## 📁 Project Structure

```
realtime-style-transfer/
├── src/
│   └── main/java/com/styletransfer/
│       └── StyleTransferApp.java     # Main application
├── models/                           # Pre-trained .t7 model files (download separately)
│   ├── vangogh.t7
│   ├── candy.t7
│   ├── starry_night.t7
│   ├── mosaic.t7
│   └── udnie.t7
├── download_models.py                # Script to auto-download models
├── pom.xml                           # Maven build config
└── README.md
```

---

## ⚙️ Setup & Installation

### Prerequisites

- **Java 11+** — [Download JDK](https://adoptium.net/)
- **Maven 3.6+** — [Download Maven](https://maven.apache.org/download.cgi)
- **Python 3** — for the model download script
- **Webcam** — any USB or built-in camera

### Step 1 — Clone the repository

```bash
git clone https://github.com/<your-username>/realtime-style-transfer.git
cd realtime-style-transfer
```

### Step 2 — Download pre-trained models

```bash
python download_models.py
```

This downloads ~6 `.t7` Torch model files into the `models/` directory (~50 MB total).

> **Manual download** (if script fails):  
> Visit https://github.com/jcjohnson/fast-neural-style and download models into `models/`.

### Step 3 — Build the project

```bash
mvn clean package -q
```

This produces a fat JAR: `target/realtime-style-transfer-1.0.0-jar-with-dependencies.jar`

### Step 4 — Run the application

```bash
java -jar target/realtime-style-transfer-1.0.0-jar-with-dependencies.jar
```

---

## 🎮 Controls

| Input | Action |
|-------|--------|
| Click style button | Switch to that painting style |
| Keys `1` – `6` | Switch styles (1 = Original, 2 = Van Gogh, ...) |
| Key `S` | Save screenshot to current directory |
| Key `Q` | Quit application |

---

## 🖼️ Available Styles

| # | Style | Inspired By |
|---|-------|------------|
| 1 | Original | No filter |
| 2 | Van Gogh | Post-Impressionist brushwork |
| 3 | Picasso | Cubist fragmentation |
| 4 | Monet | Impressionist soft color |
| 5 | Starry Night | Van Gogh's iconic swirls |
| 6 | Candy | Vivid pop-art colors |

---

## ⚡ Performance Tips

- Default resolution is **640×480** (balances quality and FPS).
- On a modern laptop CPU, expect **5–15 FPS** with style transfer enabled.
- To increase FPS, reduce resolution in `StyleTransferApp.java`:
  ```java
  private static final int IMG_W = 320;
  private static final int IMG_H = 240;
  ```
- GPU acceleration can be enabled via OpenCV's CUDA backend (advanced — see `Dnn.DNN_BACKEND_CUDA`).

---

## 🐛 Troubleshooting

| Problem | Fix |
|---------|-----|
| `Camera not found` | Check webcam is plugged in; try changing `VideoCapture(0)` to `(1)` |
| `Model not found` | Run `python download_models.py` or check the `models/` directory |
| Very low FPS | Lower resolution; close other apps; try without style first |
| Build fails | Make sure Java 11+ and Maven 3.6+ are installed |

---

## 📚 References

- Johnson et al., *Perceptual Losses for Real-Time Style Transfer* (2016)
- OpenCV DNN Module Documentation
- Fast Neural Style — https://github.com/jcjohnson/fast-neural-style

---

## 👤 Author

Aryan Desai — VIT CBtech Cse-AIML) 
Submitted for the VITyarthi Computer Vision Project  

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

