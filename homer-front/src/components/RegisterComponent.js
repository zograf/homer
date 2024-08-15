import React, { useRef, useState } from "react";
import axios from "axios";
import jwt from 'jwt-decode'
import './RegisterComponent.css'
import { API } from "../environment";

export function RegisterComponent() {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [name, setName] = useState("")
    const [repeatPassword, setRepeatPassword] = useState("")
    const [image, setImage] = useState(null)

    const [isPassword, setIsPassword] = useState(false)

    const imageRef = useRef(null)


    const handleUsername = (e) => setUsername(e.target.value);
    const handlePassword = (e) => setPassword(e.target.value);
    const handleName = (e) => setName(e.target.value);
    const handleRepeatPassword = (e) => setRepeatPassword(e.target.value);
    const handleImage = (e) => setImage(e.target.files[0]);

    const handleRegister = (e) => {
        e.preventDefault()

        let payload = new FormData()
        payload.append("image", image)
        payload.append("email", username)
        payload.append("name", name)
        payload.append("password", password)
        console.log(payload)
        console.log(image)

        axios.post(API + "/user/register", payload)
            .then(response => {
                window.location.href = "/login"
            })
            .catch(e =>
                // TODO: Popup goes here
                alert("Registration failed")
            )
    }

    const imageHandler = () => {
        imageRef.current.click()
    }

    const startAnimation = () => {
        if (isPassword) return;
        document.querySelector('.left-eye').classList.add('leftA');
        document.querySelector('.right-eye').classList.add('rightA');
        setIsPassword(true);
    };

    const stopAnimation = () => {
        if (!isPassword) return;
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
                    <h1 style={{marginBottom: "20px"}}>Register</h1>
                </div>
                <div className="flex center justify-center">
                    <img
                        alt="not found"
                        onClick={() => imageHandler()}
                        style={{width: "100px", height: "100px", borderRadius: "50%", border: "1px solid black", marginBottom: "10px"}}
                        src={image == null ? require('../img/default.png') : URL.createObjectURL(image)}
                    />
                </div>

                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">badge</span>
                    <input placeholder="Name" value={name}
                            onChange={handleName}
                            onFocus={() => {stopAnimation(); setIsPassword(false);}}
                    />
                </div>
                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">mail</span>
                    <input placeholder="Email" value={username}
                            onChange={handleUsername}
                            onFocus={() => {stopAnimation(); setIsPassword(false)}}
                    />
                </div>
                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">key</span>
                    <input placeholder="Password" type="Password" value={password}
                            onChange={handlePassword}
                            onFocus={startAnimation}/>
                </div>
                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">lock</span>
                    <input placeholder="Repeat password" type="Password" value={repeatPassword}
                            onChange={handleRepeatPassword}
                            onFocus={startAnimation}/>
                </div>
                <div className="v-spacer-s">
                    <a href="/login" className="login-link">Already have an account?</a>
                </div>
                <div className='flex gap-xs justify-center'>
                    <button className='small-button solid-accent-button' onClick={handleRegister}>Register</button>
                </div>
                <input
                    style={{display: "none"}}
                    type="file"
                    name="image"
                    onChange={handleImage}
                    ref={imageRef}>
                </input>
            </div>
        </div>
    )
}
