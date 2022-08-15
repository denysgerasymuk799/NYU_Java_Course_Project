/* eslint-disable jsx-a11y/anchor-is-valid */

const NavDropdown = () => {
  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      console.log('tokens removed')
    } catch (error) {
      console.error(error)
    }
    window.location.href = '/';
  }

  return (
    <div className="dropdown-menu dropdown-menu-end bank-dropdown">
        <a href="#" className="dropdown-item" onClick={handleLogout}>
          Exit
          <i className="fa fa-sign-out ml-75" aria-hidden="true"></i>
        </a>
    </div>
  )
}

export default NavDropdown
