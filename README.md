# 🧠 ChatGPT IntelliJ Assistant

A smart coding assistant for IntelliJ IDEA that lets you refactor, clean, optimize, and rework selected code using OpenAI's GPT models directly from your editor.

---

## ⚙️ Setup

1. **Get an OpenAI API key**  
   Sign up at [https://platform.openai.com](https://platform.openai.com) and generate a key.

2. **Add API key to your system environment (Windows)**  
   Open Command Prompt and run:

   ```bash
   setx OPENAI_API_KEY "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
   ```

   > 🔁 **Restart IntelliJ** after running this so it picks up the environment variable.

---

## 🚀 Usage

1. Select any code in the editor.
2. Right-click and choose **"Send to ChatGPT"**.
3. In the popup:
   - Customize your prompt.
   - Use checkboxes for default tasks: Refactor, Optimize, Rework.
   - View the selected model (auto-detected).
4. Choose one of the result actions:
   - ✅ **Apply** the suggestion directly to the code.
   - 📋 **Copy to Clipboard**.
   - 🔁 **Reiterate** with a new prompt.

---

## 🧠 How It Works

- Detects best available GPT model (`gpt-4`, `gpt-3.5-turbo`, etc.).
- Uses `OkHttp` and `org.json` to communicate with the OpenAI API.
- Responses are parsed and displayed cleanly.
- Model and plugin logic is separated into:
  - `ChatGPTClient.kt`
  - `SendToChatGPTAction.kt`
  - `ChatGptPromptDialog.kt`

---

## 🛠 Built With

- Kotlin
- IntelliJ SDK
- Gradle + Kotlin DSL
- OpenAI GPT API
- Swing UI Components

---

## 🤓 Example Use Cases

- Refactor legacy Vue components
- Clean up Spring Boot controllers
- Optimize performance-critical loops
- Rework REST APIs for clarity
- Generate better variable/function names

---

## 🧑‍💻 Author

**Tom Descheemaeker**  
[GitHub](https://github.com/TomDesch)

---

## 📃 License

MIT (or add your own)

---

> ✨ Want to improve it? Fork the repo and start hacking!
