# Readme

## What is this?

Open Banking Brasil Mock Payments Client

### Getting started

1. Install mongoDB and Docker if you don't have them installed

2. Add the following mappings to your /etc/hosts file:


- **127.0.0.1   &nbsp; &nbsp; &nbsp; &nbsp;    mongo1**
- **127.0.0.1   &nbsp; &nbsp; &nbsp; &nbsp;    mongo2**
- **127.0.0.1   &nbsp; &nbsp; &nbsp; &nbsp;    mongo3**

3. To do step 2, follow these instructions:
    1. Launch Terminal
    2. Type the following command at the prompt:

    ```
    sudo nano /private/etc/hosts
    ```

    3. sudo nano /private/etc/hosts

    4. Enter the administrator password when requested
    5. Once the hosts file is loaded within nano, use the arrow keys to navigate to the bottom of the hosts file to make your modifications
    6. When finished, hit Control+O followed by ENTER/RETURN to save changes to /private/etc/hosts, then hit Control+X to exit out of nano

4. Run the following command to install all the required packages

```
npm install
```

5. Open docker and run the following command in your terminal:

```
docker-compose up
```

6. Run the following command to start the front-end and the back-end both at the same time

```
npm start
```

Setep 7 and 8 are alternative to step 6 (optional)
7. If you want to run the front-end and back-end on seperate terminals (get colourful logs), run the following commands:
Front-End

```
npm run serve
```

8. Back-End

```
DEBUG=tpp* node index.js
```

Open a Browser to <https://tpp.localhost>

- You will need to add a local dns entry in your hosts file
