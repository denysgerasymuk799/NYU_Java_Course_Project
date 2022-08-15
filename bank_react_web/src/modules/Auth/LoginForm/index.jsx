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

const LoginForm = () => {
  const history = useHistory()
  const classes = useStyles();

  const [formState, setFormState] = useState({
    username: 'denys20220728_1',
    password: 'secret',
    error: '',
  })

  useEffect(() => {
    const accessToken = localStorage.getItem('access_token')
    const refreshToken = localStorage.getItem('refresh_token')
    console.log('tokens', accessToken, refreshToken)
    if (accessToken) {
      try {
        history.push('/profile')
      } catch (error) {
        console.error(error)
        setFormState({ ...formState, error })
      }
    }
  }, [formState, history])

  const handleChange = e => {
    setFormState({ ...formState, [e.target.name]: e.target.value })
  }

  const handleSubmit = async e => {
    e.preventDefault()
    const { username, password } = formState

    const jsonData = {
      "username": username,
      "password": password
    }
    
    await api
      .login(jsonData)
      .then(apiResponse => {
        console.log('apiResponse', apiResponse.data)
        const accessToken = apiResponse.data.token
        const refreshToken = apiResponse.data.refresh_token
        console.log('accessToken, refreshToken', accessToken, refreshToken)
        console.log('username', apiResponse.data.username)

        localStorage.setItem('access_token', accessToken)
        localStorage.setItem('refresh_token', refreshToken)
        localStorage.setItem('username', apiResponse.data.username)
        localStorage.setItem('card_id', apiResponse.data.card_id)
        localStorage.setItem('email', apiResponse.data.email)
        history.go('/profile')
      })
      .catch(error => {
        console.log('error', error)
        setFormState({ ...formState, error: 'Error has occurred: ' + error })
      })
  }

  return (
    <section id="right-section">
      <form noValidate>
        <h3>Sign in</h3>
        <p className={styles.message}>{formState.error}</p>
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
          label="Password"
          name="password"
          type="password"
          variant="outlined"
          className={styles.nomoInput + " " + classes.root}
          value={formState.password}
          onChange={e => handleChange(e)}
        />
        <button 
          className="bank-btn black login-btn"
          onClick={e => handleSubmit(e)}
        >
          <span>Continue</span>
        </button>
        <p className={styles.additionalText}>
          Don't have an account? <a href="/sign_up">Sign up</a>
        </p>
      </form>
    </section>
  )
}

export default LoginForm
