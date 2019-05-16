<html>

<head>
    <title>Region Midt login</title>
</head>

<style>
    *,
    *::before,
    *::after {
        border: none;
        -webkit-box-sizing: border-box;
                box-sizing: border-box;
        margin: 0;
        padding: 0;
    }

    html {
        font: normal normal normal 16px / 1.3 sans-serif;
    }

    body {
        -webkit-box-align: center;
            -ms-flex-align: center;
                align-items: center;
        background-color: #fff;
        display: -webkit-box;
        display: -ms-flexbox;
        display: flex;
        -webkit-box-pack: center;
            -ms-flex-pack: center;
                justify-content: center;
        min-height: 100vh;
    }

    a {
        color: inherit;
        text-decoration: none;
    }

    .login {
        background-color: rgba(0, 0, 0, 0.05);
        border: 1px solid rgba(0, 0, 0, 0.1);
        border-radius: 3px;
        -webkit-box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        padding: 2rem;
        max-width: 20rem;
    }

    .login__form {
        margin-top: 2rem;
    }

    .login__label {
        display: block;
        font-size: 0.8rem;
    }

    .login__input {
        background-color: #e8f0fe;
        border: 1px solid rgba(0, 0, 0, 0.2);
        border-radius: 3px;
        font-size: 1rem;
        padding: 0.5rem;
        width: 100%;
    }

    .login__input:focus {
        border: 1px solid rgba(0, 0, 0, 0.4);
    }

    .login__input + .login__label,
    .login__input + .login__submit {
        margin-top: 1rem;
    }

    .login__submit {
        background-color: rgba(0, 0, 0, 0.1);
        border: 1px solid rgba(0, 0, 0, 0.1);
        border-radius: 3px;
        display: inline-block;
        font-size: 1rem;
        padding: 0.5rem 1rem;
        cursor: pointer;
        -webkit-transition: border 0.2s ease-in-out;
        -o-transition: border 0.2s ease-in-out;
        transition: border 0.2s ease-in-out;
    }

    .login__submit:focus,
    .login__submit:hover {
        border: 1px solid rgba(0, 0, 0, 0.4);
    }

    .footer {
        font-size: 0.8rem;
        margin-top: 1rem;
    }
</style>

<body>

    <div class="login">
        <img src="${url.context}/res/login/rm.png" class="login__logo" height="62" width="128">

        <form id="loginform" accept-charset="UTF-8" method="post" action="${url.context}/page/dologin" class="login__form">
            <label for="username" class="login__label">regionsID:</label>
            <input type="text" id="username" name="username" class="login__input" autofocus="autofocus" />

            <label for="password" class="login__label">Adgangskode:</label>
            <input type="password" id="password" name="password" class="login__input" />

            <button type="submit" class="login__submit">Log ind</button>
        </form>

        <p class="footer">
            <a href="https://bsk.rm.dk/">Hjælp, jeg har glemt / kender ikke min adgangskode</a>
        </p>
    </div>
</body>
</div1>

</html>




