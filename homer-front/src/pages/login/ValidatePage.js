import { ValidateComponent } from '../../components/ValidateComponent'
import { useLocation } from 'react-router-dom';
import './ValidatePage.css'
import { useMemo } from 'react';

export function ValidatePage() {

    return(
        <main className="mh-100">
            <div className="flex center justify-center validate-card">
                <ValidateComponent/>

            </div>
        </main>
    )
}
