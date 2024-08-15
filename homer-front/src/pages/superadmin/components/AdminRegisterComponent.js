import React, { useRef, useState } from "react";
import axios from "axios";
import jwt from 'jwt-decode'
import './AdminRegisterComponent.css'
import { API } from "../../../environment";

export default function AdminRegisterComponent() {
    const token = localStorage.getItem("token")

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [name, setName] = useState("")
    const [repeatPassword, setRepeatPassword] = useState("")
    const [image, setImage] = useState(null)

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

        axios.post(API + "/admin/register", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                // TODO: Popup goes here
                //alert("Registration successful")
                console.log("Registration successful")
                window.location.reload()
            })
            .catch(e =>
                // TODO: Popup goes here
                //alert("Registration failed")
                console.log("Registration failed")
            )
    }

    const imageHandler = () => {
        imageRef.current.click()
    }

    return (
        <div className="wrapper gap-s">
            <div>
                <div className="card v-spacer-s">
                    <div className="flex center justify-center v-spacer-m">
                        <img
                            alt="not found"
                            onClick={() => imageHandler()}
                            style={{ width: "125px", height: "125px", borderRadius: "50%", border: "1px solid black", marginBottom: "10px" }}
                            src={image == null ? require('../../../img/default.png') : URL.createObjectURL(image)}
                        />
                    </div>

                    <div className="input-wrapper regular-border v-spacer-xs">
                        <span className="material-symbols-outlined icon input-icon">badge</span>
                        <input placeholder="Name" value={name}
                            onChange={handleName}
                        />
                    </div>
                    <div className="input-wrapper regular-border v-spacer-xs">
                        <span className="material-symbols-outlined icon input-icon">mail</span>
                        <input placeholder="Email" value={username}
                            onChange={handleUsername}
                        />
                    </div>
                    <div className="input-wrapper regular-border v-spacer-xs">
                        <span className="material-symbols-outlined icon input-icon">key</span>
                        <input placeholder="Password" type="Password" value={password}
                            onChange={handlePassword}
                        />
                    </div>
                    <div className="input-wrapper regular-border v-spacer-xs">
                        <span className="material-symbols-outlined icon input-icon">lock</span>
                        <input placeholder="Repeat password" type="Password" value={repeatPassword}
                            onChange={handleRepeatPassword}
                        />
                    </div>
                    <input
                        style={{ display: "none" }}
                        type="file"
                        name="image"
                        onChange={handleImage}
                        ref={imageRef}>
                    </input>
                </div>
                <div className='flex gap-xs justify-end'>
                    <button className='small-button solid-accent-button' onClick={handleRegister}>Register</button>
                </div>
            </div>
        </div>
    )
}