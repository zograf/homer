import './ValidateComponent.css'
import { useLocation } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { API } from "../environment";
import axios from "axios";

export function ValidateComponent() {
    const location = useLocation();
    const [message, setMessage] = useState("Verifying...")

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');

        console.log(token)
        if (token == null) {
            setMessage("Verification failed! Invalid token")
            return
        }

        axios.get(API + `/user/activate/${token}`)
            .then(response => {
                setMessage("Verification successful! Redirecting you to login...")
                setTimeout(() => {
                    window.location.href = '/login';
                }, 3000);
            })
            .catch(e =>
                setMessage("Verification failed! Invalid token")
            )
    }, []);

    return(
        <div>
            <div className="card">
                <div className="flex center justify-center">
                    <h1 style={{marginBottom: "20px"}}>Verifying</h1>
                </div>
                <div className="center justify-center v-spacer-xs">
                    {message}
                </div>
            </div>
        </div>
    )
}
