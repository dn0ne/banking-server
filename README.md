# Server for [Banking App](https://github.com/dn0ne/banking-app)

## Build
1. **Get the Source Code**  
   - Clone the repository or download the source code:
     ```bash
     git clone https://github.com/dn0ne/banking-server.git
     ```

2. **Open project in IDE**  
   - Launch your IDE.  
   - Select **File > Open** and navigate to the project's folder.  
   - Click **OK** to open the project.
  
3. **Start database**
   - Start Docker
   - Execute in terminal:
     ```
     ./database/start-db.sh (on Linux or WSL)
     or
     .\database\start-db.bat (on Windows)
     ```
  
4. **Configure mail server access**
   - In the project's root folder create `mail.env` file with the following contents:
     ```env
     MAIL_HOST="smtp.gmail.com"
     MAIL_PORT=587
     MAIL_USERNAME="your username here"
     MAIL_PASSWORD="your password here"
     ```

4. **Run the project**  
   - Wait for the project to sync and build (Gradle sync may take some time).  
   - Click the **Run** button to build and launch the server.  
