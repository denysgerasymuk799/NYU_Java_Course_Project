/* eslint-disable no-console */
/* eslint-disable no-undef */
import TextField from '@material-ui/core/TextField'
import React, { useState, useEffect } from 'react'
import { useHistory } from 'react-router-dom'
import api from '../../../api'
import styles from './styles.module.scss'
import { makeStyles } from "@material-ui/core/styles";


const useStyles = makeStyles({
  root: {
    "& .MuiOutlinedInput-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
      borderColor: "#6D3ADC"
    },
    "& .MuiInputLabel-outlined.Mui-focused": {
      color: "#6D3ADC"
    }
  }
});

const RegistrationForm = () => {
  const history = useHistory()
  const classes = useStyles();

  const [formState, setFormState] = useState({
    firstname: '',
    lastname: '',
    username: '',
    email: '',
    password: '',
    repeat_password: '',
    error: '',
  })

  useEffect(() => {
    const accessToken = localStorage.getItem('access_token')
    const refreshToken = localStorage.getItem('refresh_token')
    console.log('tokens', accessToken, refreshToken)
    if (accessToken) {
      window.location.href = '/profile';
    }
  }, [formState, history])

  const handleOnlyLetters = e => {
    const onlyLetters = e.target.value.replace(/[^a-zA-Z]/g, '');
    setFormState({ ...formState, [e.target.name]: onlyLetters })
  }

  const handleChange = e => {
    setFormState({ ...formState, [e.target.name]: e.target.value })
  }

  function validate(json) {
    for(var field of Object.entries(json)) {
      if (field[1] === "") {
        return [false, 'One or more fields are missing'];
      }
    }
    const emailPattern = /[a-zA-Z0-9]+[\.]?([a-zA-Z0-9]+)?[\@][a-z]{3,9}[\.][a-z]{2,5}/g;
    const result = emailPattern.test(json['email']);
    if(!result){ return [false, 'Wrong email'] };

    if (json['password'].length < 4) { return [false, 'Password too short'] }

    return [true, ''];
  }

  const handleSubmit = async e => {
    e.preventDefault();

    const { firstname, lastname, username, email, password, repeat_password } = formState

    const jsonData = {
      "firstname": firstname,
      "lastname": lastname,
      "username": username,
      "email": email,
      "password": password,
      "repeat_password": repeat_password
    }

    const [validResult, errorText] = validate(jsonData);

    console.log("validResult", validResult, errorText);
    
    if (!validResult) { return setFormState({ ...formState, 'error': errorText }) }

    await api
      .register(jsonData)
      .then(apiResponse => {
        console.log('apiResponse', apiResponse.data)
        window.location.href = '/login';
      })
      .catch(error => {
        console.log('error', error)
        setFormState({ ...formState, error: 'Error has occurred: ' + error })
      })
  }

  return (
    <section id="right-section">
      <form>
        <h3>Sign up</h3>
        <p className={styles.message}>{formState.error}</p>
        <TextField
          fullWidth
          label="First name"
          name="firstname"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.firstname}
          onChange={e => handleOnlyLetters(e)}
        />
        <TextField
          fullWidth
          label="Last name"
          name="lastname"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.lastname}
          onChange={e => handleOnlyLetters(e)}
        />
        <TextField
          fullWidth
          label="Username"
          name="username"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.username}
          onChange={e => handleChange(e)}
        />
        <TextField
          fullWidth
          label="E-mail"
          name="email"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.email}
          onChange={e => handleChange(e)}
        />
        <TextField
          fullWidth
          label="Password"
          name="password"
          type="password"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.password}
          onChange={e => handleChange(e)}
        />
        <TextField
          fullWidth
          label="Password repeat"
          name="repeat_password"
          type="password"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.repeat_password}
          onChange={e => handleChange(e)}
        />
        <button 
          className="bank-btn black login-btn"
          onClick={e => handleSubmit(e)}
        >
          <span>Continue</span>
        </button>
        <p className={styles.additionalText}>
          Do you have an account? <a href="/">Sign in</a>
        </p>
      </form>
    </section>
  )
}

export default RegistrationForm
