import React, { useState, useMemo } from "react";
import axios from "axios";
import jwt from 'jwt-decode'
import './LoginComponent.css'
import { API } from "../environment";

export function LoginComponent() {

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")

    const handleUsername = (e) => setUsername(e.target.value);
    const handlePassword = (e) => setPassword(e.target.value);

    useMemo(() => {
        localStorage.setItem("token", "")
        localStorage.setItem("username", "")
        localStorage.setItem("id", "")
        localStorage.setItem("isUser", false)
        localStorage.setItem("isSuperAdmin", false)
    }, [])

    const handleSubmit = (e) => {
        e.preventDefault()
        let payload = { "email": username, "password": password }

        axios.post(API + "/user/login", payload)
            .then(response => {
                localStorage.setItem("token", response.data.accessToken)
                localStorage.setItem("username", response.data.email)
                localStorage.setItem("id", response.data.userId)
                localStorage.setItem("isUser", response.data.userRole == "ROLE_USER")
                localStorage.setItem("isSuperAdmin", response.data.userRole == "ROLE_SUPER_ADMIN")
                console.log(response);
                if (response.data.userRole == "ROLE_USER")
                    window.location.href = '/devices'
                else
                    window.location.href = '/property/requests'
            })
            .catch(e =>
                //TODO: Popup goes here
                alert("Incorrect combination of username and password")
            )
    }

    const startAnimation = () => {
        document.querySelector('.left-eye').classList.add('leftA');
        document.querySelector('.right-eye').classList.add('rightA');
    };

    const stopAnimation = () => {
        document.querySelector('.left-eye').classList.remove('leftA');
        document.querySelector('.right-eye').classList.remove('rightA');
        document.querySelector('.left-eye').classList.add('leftReverseA');
        document.querySelector('.right-eye').classList.add('rightReverseA');
    };

    return(
        <div>
            <div className={`left-eye`}>
            </div>
            <div className={`right-eye`}>
            </div>
            <div className="card">
                <div className="flex center justify-center">
                    <h1 style={{marginBottom: "20px"}}>Login</h1>
                </div>
                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">mail</span>
                    <input placeholder="Username" value={username} onChange={handleUsername} />
                </div>
                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">key</span>
                    <input placeholder="Password" type="Password" value={password} onChange={handlePassword}
                            onFocus={startAnimation}
                            onBlur={stopAnimation}/>
                </div>
                <div className="v-spacer-s">
                    <a href="/register" className="register-link">Don't have an account?</a>
                </div>
                <div className='flex gap-xs justify-center'>
                    <button className='small-button solid-accent-button' onClick={handleSubmit}>Login</button>
                </div>
            </div>
        </div>
    )
}
