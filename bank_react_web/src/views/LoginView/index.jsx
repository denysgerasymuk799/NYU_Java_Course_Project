/* eslint-disable jsx-a11y/media-has-caption */
import React from 'react'
import LoginForm from '../../modules/Auth/LoginForm'

const LoginView = () => (
  <div>
    <section id="left-section">
      <h1 className="main-logo login-logo">
        <a href="index.html">
          unobank
          <span className="gray">| Ukrainian Catholic University</span>
        </a>
      </h1>
      <h2>Спробуйте новий веб-банкінг зі зручним інтерфейсом</h2>
      <img alt="" src="./img/qr-cat.svg" />
    </section>

    <LoginForm />
  </div>
)

export default LoginView