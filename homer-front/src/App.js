import './App.css';
import AddPropertyPage from './pages/properties/add/AddPropertyPage';
import PropertyRequestsPage from './pages/properties/requests/PropertyRequestsPage'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { LoginPage } from './pages/login/LoginPage'
import { RegisterPage } from './pages/login/RegisterPage'
import { ValidatePage } from './pages/login/ValidatePage'
import PropertyPage from "./pages/devices/PropertyPage";
import AddDevicePage from './pages/devices/add/AddDevicePage';
import DevicesPage from './pages/devices/DevicesPage';
import AdminRegisterPage from './pages/superadmin/AdminRegisterPage';
import {UserConsumptionPage, AdminConsumptionPage} from './pages/properties/consumption/PropertyConsumption';
import SharedPage from './pages/shared/SharedPage';

function App() {
    return (
        <main>
            <Router>
                <Routes>
                    <Route exact path='/property/add' element={<AddPropertyPage />} />
                    <Route exact path='/property/requests' element={<PropertyRequestsPage />} />
                    <Route exact path='/property/consumption' element={<UserConsumptionPage/>} />
                    <Route exact path='/devices' element={<PropertyPage />} />
                    <Route path='/devices/:propertyId/:shared' element={<DevicesPage />} />
                    <Route exact path='/device/add' element={<AddDevicePage />}/>
                    <Route exact path='/user/shared' element={<SharedPage />}/>

                    <Route exact path='/admin/register' element={<AdminRegisterPage />} />
                    <Route exact path='/admin/consumption' element={<AdminConsumptionPage />} />

                    <Route exact path='/' element={<LoginPage />} />
                    <Route exact path='/login' element={<LoginPage />} />
                    <Route exact path='/register' element={<RegisterPage />} />
                    <Route exact path='/validate' element={<ValidatePage />} />
                </Routes>
            </Router>
        </main>
    );
}

export default App;
