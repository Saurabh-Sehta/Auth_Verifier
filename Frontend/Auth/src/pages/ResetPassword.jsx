import React, { useContext, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { assets } from '../assets/assets'
import axios from 'axios';
import { AppContext } from '../context/AppContext';
import { toast } from 'react-toastify';

const ResetPassword = () => {
  const inputRef = useRef([]);
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [isEmailSent, setIsEmailSent] = useState(false);
  const [otp, setOtp] = useState("");
  const [isOtpSubmitted, setIsOtpSubmitted] = useState(false);
  const { getUserData, isLoggedIn, userData, backendURL } = useContext(AppContext);
  const navigate = useNavigate();

  axios.defaults.withCredentials = true;

  const handleChange = (e, index) => {
    const value = e.target.value.replace(/\D/, "");
    e.target.value = value;
    if(value && index < 5){
      inputRef.current[index + 1].focus();
    }
  }

  const handleKeyDown = (e, index) => {
    if(e.key === "Backspace" && !e.target.value && index > 0){
      inputRef.current[index - 1].focus();
    }
  }

  const handlePaste = (e) => {
    e.preventDefault();
    const paste = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6).split("");
    paste.forEach((digit, i) => {
      if (inputRef.current[i]) {
        inputRef.current[i].value = digit;
      }
    });
    const next = paste.length < 6 ? paste.length : 5;
    inputRef.current[next].focus();
  }

  const onEmailSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`${backendURL}/send-reset-otp?email=`+email);
      if(response.status === 200){
        toast.success("OTP sent to your email");
        setLoading(true);
        setIsEmailSent(true);
      } else{
        toast.error("Unable to send OTP. Please try again.");
      }
    } catch (err) {
      toast.error("Something went wrong! Please try again later.");
      console.log(err);
    } finally {
      setLoading(false);
    }
  }

  return (
    
    <>
      <nav className='navbar bg-white px-5 py-4 d-flex justify-content-between align-items-center' >
        <Link to="/" className='d-flex align-items-center gap-2' style={{ textDecoration: "none" }}>
          <img src={assets.logo} alt="logo" width={32} height={32} />
          <span className='fw-bold fs-4 text-dark'>Auth-Verifer</span>
        </Link>
      </nav>

      <div className='email-verify-container d-flex align-items-center justify-content-center vh-100 position-relative'
        style={{
          background: "linear-gradient(90deg, #6a5af9, #8268f9)",
          border: "none"
        }}
      >
        {/* Reset password card */}
        {!isEmailSent && (
          <div className='p-5 rounded-4 shadow bg-white text-center' style={{ width: "100%", maxWidth: "400px" }}>
            <h4 className='fw-bold mb-4'>Reset Password</h4>
            <p className='mb-4'>Enter your registered email address</p>
            <form onSubmit={onEmailSubmit}>
              <div className='input-group mb-4 bg-secondary bg-opacity-10 rounded-pill'>
                <span className='input-group-text bg-transparent border-0 ps-4'>
                  <i className='bi bi-envelope'></i>
                </span>
                <input
                  type="email"
                  className='form-control bg-transparent border-0 ps-1 pe-4 rounded-end'
                  placeholder='Enter email address'
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <button type='submit' className='btn btn-primary w-100 py-2' disabled={loading}>
                Submit
              </button>
            </form>
          </div>)}
        {/* OTP Card */}
        {isEmailSent && !isOtpSubmitted && (
          <div className='p-5 rounded-4 shadow bg-white' style={{ width: "400px" }}>
            <h4 className='text-center fw-bold mb-2'>Email Verify OTP</h4>
            <p className='text-center mb-4'>
              Enter the 6-digit code sent to your email.
            </p>

            <div className='d-flex justify-content-between gap-2 mb-4 text-text-center text-white mb-2'>
              {[...Array(6)].map((_, i) => (
                <input
                  key={i}
                  type="text"
                  maxLength={1}
                  className='form-control text-center fs-4 otp-input'
                  ref={(el) => (inputRef.current[i] = el)}
                  onChange={(e) => handleChange(e, i)}
                  onKeyDown={(e) => handleKeyDown(e, i)}
                  onPaste={handlePaste}
                />
              ))}
            </div>

            <button className='btn btn-primary w-100 fw-semibold' disabled={loading}>
              {loading ? "Verifying..." : "Verify email"}
            </button>
          </div>
        )}

        {/* new password form */}
              {isOtpSubmitted && isEmailSent && (
                <div className='rounded-4 p-4 text-center bg-white'
                  style={{
                    width: "100%",
                    maxWidth: "400px"
                  }}
                >
                    <h4>New Password</h4>
                    <p className='mb-4'>Enter your new password below</p>
                    <form>
                      <div className='input-group mb-4 bg-secondary bg-opacity-10 rounded-pill'>
                        <span className='input-group-text bg-transparent border-0 ps-4'>
                          <i className='bi bi-person-fill-lock'></i>
                        </span>
                        <input 
                          type="password" 
                          className='form-control bg-transparent border-0 ps-1 pe-4 rounded-end' 
                          placeholder='**********'
                          onChange={(e) => setNewPassword(e.target.value)}
                          value={newPassword}
                          required 
                        />
                      </div>
                      <button type='submit' className='btn btn-primary w-100 py-2'>
                        {loading ? "Submitting..." : "Submit"}
                      </button>
                    </form>
                </div>
                
              )}
      </div>
    </>
  )
}

export default ResetPassword