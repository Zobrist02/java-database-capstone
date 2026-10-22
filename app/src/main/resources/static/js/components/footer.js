function renderFooter() {
  const footerContainer = document.getElementById("footer");
  if (!footerContainer) return;

  footerContainer.innerHTML = `
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-logo">
          <img src="/assets/images/logo/logo.png" alt="Smart Clinic logo">
          <p>© 2026 Smart Clinic Management System.</p>
        </div>

        <div class="footer-links">
          <section class="footer-column">
            <h2>Company</h2>
            <a href="#">About</a>
            <a href="#">Careers</a>
            <a href="#">Press</a>
          </section>
          <section class="footer-column">
            <h2>Support</h2>
            <a href="#">Account</a>
            <a href="#">Help Center</a>
            <a href="#">Contact</a>
          </section>
          <section class="footer-column">
            <h2>Legal</h2>
            <a href="#">Terms</a>
            <a href="#">Privacy</a>
            <a href="#">Licensing</a>
          </section>
        </div>
      </div>
    </footer>
  `;
}

renderFooter();