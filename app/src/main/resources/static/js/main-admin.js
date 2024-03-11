(function () {
  "use strict";

  /**
 * Defaults for initial first question
 */
  // document.addEventListener("DOMContentLoaded", showProperButtons);
  showProperButtons();


  /**
   * Easy selector helper function
   */
  const select = (el, all = false) => {
    el = el.trim()
    if (all) {
      return [...document.querySelectorAll(el)]
    } else {
      return document.querySelector(el)
    }
  }

  /**
   * Easy event listener function
   */
  const on = (type, el, listener, all = false) => {
    let selectEl = select(el, all)
    if (selectEl) {
      if (all) {
        selectEl.forEach(e => e.addEventListener(type, listener))
      } else {
        selectEl.addEventListener(type, listener)
      }
    }
  }

  /**
   * Easy on scroll event listener
   */
  const onscroll = (el, listener) => {
    el.addEventListener('scroll', listener)
  }

  /**
   * Navbar links active state on scroll
   */
  let navbarlinks = select('#navbar .scrollto', true)
  const navbarlinksActive = () => {
    let position = window.scrollY + 200
    navbarlinks.forEach(navbarlink => {
      if (!navbarlink.hash) return
      let section = select(navbarlink.hash)
      if (!section) return
      if (position >= section.offsetTop && position <= (section.offsetTop + section.offsetHeight)) {
        navbarlink.classList.add('active')
      } else {
        navbarlink.classList.remove('active')
      }
    })
  }
  window.addEventListener('load', navbarlinksActive)
  onscroll(document, navbarlinksActive)

  /**
   * Scrolls to an element with header offset
   */
  const scrollto = (el) => {
    let elementPos = select(el).offsetTop
    window.scrollTo({
      top: elementPos,
      behavior: 'smooth'
    })
  }

  /**
   * Back to top button
   */
  let backtotop = select('.back-to-top')
  if (backtotop) {
    const toggleBacktotop = () => {
      if (window.scrollY > 100) {
        backtotop.classList.add('active')
      } else {
        backtotop.classList.remove('active')
      }
    }
    window.addEventListener('load', toggleBacktotop)
    onscroll(document, toggleBacktotop)
  }

  /**
   * Mobile nav toggle
   */
  on('click', '.mobile-nav-toggle', function (e) {
    select('body').classList.toggle('mobile-nav-active')
    this.classList.toggle('bi-list')
    this.classList.toggle('bi-x')
  })

  /**
   * Scrool with ofset on links with a class name .scrollto
   */
  on('click', '.scrollto', function (e) {
    if (select(this.hash)) {
      e.preventDefault()

      let body = select('body')
      if (body.classList.contains('mobile-nav-active')) {
        body.classList.remove('mobile-nav-active')
        let navbarToggle = select('.mobile-nav-toggle')
        navbarToggle.classList.toggle('bi-list')
        navbarToggle.classList.toggle('bi-x')
      }
      scrollto(this.hash)
    }
  }, true)

  /**
   * Scroll with ofset on page load with hash links in the url
   */
  window.addEventListener('load', () => {
    if (window.location.hash) {
      if (select(window.location.hash)) {
        scrollto(window.location.hash)
      }
    }
  });

  /**
   * Hero type effect
   */
  const typed = select('.typed')
  if (typed) {
    let typed_strings = typed.getAttribute('data-typed-items')
    typed_strings = typed_strings.split(',')
    new Typed('.typed', {
      strings: typed_strings,
      loop: true,
      typeSpeed: 100,
      backSpeed: 50,
      backDelay: 2000
    });
  }

  /**
   * Skills animation
   */
  let skilsContent = select('.skills-content');
  if (skilsContent) {
    new Waypoint({
      element: skilsContent,
      offset: '80%',
      handler: function (direction) {
        let progress = select('.progress .progress-bar', true);
        progress.forEach((el) => {
          el.style.width = el.getAttribute('aria-valuenow') + '%'
        });
      }
    })
  }

  /**
   * Porfolio isotope and filter
   */
  window.addEventListener('load', () => {
    let portfolioContainer = select('.portfolio-container1');
    if (portfolioContainer) {
      let portfolioIsotope = new Isotope(portfolioContainer, {
        itemSelector: '.portfolio-item'
      });

      let portfolioFilters = select('#portfolio-flters li', true);

      on('click', '#portfolio-flters li', function (e) {
        e.preventDefault();
        portfolioFilters.forEach(function (el) {
          el.classList.remove('filter-active');
        });
        this.classList.add('filter-active');

        portfolioIsotope.arrange({
          filter: this.getAttribute('data-filter')
        });
        portfolioIsotope.on('arrangeComplete', function () {
          AOS.refresh()
        });
      }, true);
    }

  });

  /**
   * Porfolio isotope and filter
   */
  window.addEventListener('load', () => {
    let portfolioContainer = select('.portfolio-container2');
    if (portfolioContainer) {
      let portfolioIsotope = new Isotope(portfolioContainer, {
        itemSelector: '.portfolio-item'
      });

      let portfolioFilters = select('#portfolio-flters li', true);

      on('click', '#portfolio-flters li', function (e) {
        e.preventDefault();
        portfolioFilters.forEach(function (el) {
          el.classList.remove('filter-active');
        });
        this.classList.add('filter-active');

        portfolioIsotope.arrange({
          filter: this.getAttribute('data-filter')
        });
        portfolioIsotope.on('arrangeComplete', function () {
          AOS.refresh()
        });
      }, true);
    }

  });

  /**
   * Porfolio isotope and filter
   */
  window.addEventListener('load', () => {
    let portfolioContainer = select('.portfolio-container3');
    if (portfolioContainer) {
      let portfolioIsotope = new Isotope(portfolioContainer, {
        itemSelector: '.portfolio-item'
      });

      let portfolioFilters = select('#portfolio-flters li', true);

      on('click', '#portfolio-flters li', function (e) {
        e.preventDefault();
        portfolioFilters.forEach(function (el) {
          el.classList.remove('filter-active');
        });
        this.classList.add('filter-active');

        portfolioIsotope.arrange({
          filter: this.getAttribute('data-filter')
        });
        portfolioIsotope.on('arrangeComplete', function () {
          AOS.refresh()
        });
      }, true);
    }

  });


  /**
   * Portfolio details slider
   */
  new Swiper('.portfolio-details-slider', {
    speed: 400,
    loop: true,
    autoplay: {
      delay: 5000,
      disableOnInteraction: false
    },
    pagination: {
      el: '.swiper-pagination',
      type: 'bullets',
      clickable: true
    }
  });

  /**
   * Testimonials slider
   */
  new Swiper('.testimonials-slider', {
    speed: 600,
    loop: true,
    autoplay: {
      delay: 5000,
      disableOnInteraction: false
    },
    slidesPerView: 'auto',
    pagination: {
      el: '.swiper-pagination',
      type: 'bullets',
      clickable: true
    },
    breakpoints: {
      320: {
        slidesPerView: 1,
        spaceBetween: 20
      },

      1200: {
        slidesPerView: 3,
        spaceBetween: 20
      }
    }
  });


})()

/**
 * Defaults for initial first question
 */


/**
 * shows the quiz submit form 
 */
function onClickSubmitButtonRequest() {
  document.getElementById("submit_quiz_form").style.removeProperty('display');

  $('html,body').animate({
    scrollTop: $(".formSubmitQuiz").offset().top
  }, 'slow');

}

/**
 * hides the quiz submit form 
 */
function hideSubmitQuizForm() {
  document.getElementById("submit_quiz_form").style.display = "none";
}




function showDesiredButton(currentQuestion, totalQuestion) {

  if (currentQuestion == totalQuestion) {
    console.log("reached the last question, lets show submit button");
    document.getElementById("nextQuestionButton").innerHTML = "Submit Quiz";
  } else {
    document.getElementById("nextQuestionButton").innerHTML = "Next";
  }

  if (currentQuestion == 1) {
    document.getElementById("previousQuestionButton").disabled = true;
  } else {
    document.getElementById("previousQuestionButton").disabled = false;
  }

}

function showDesiredNumberOnInfo(currentQuestion, totalQuestion) {
  document.getElementById("questionNumberInfo").innerHTML = "(" + currentQuestion + " of " + totalQuestion + ")";
}







/**
 * taking user input when he clicks the options 
 */
function optionSelected(selectedOption) {
  let currentQuestionValue = Number(document.getElementById("questionNumberToShow").value);
  console.log("currentQuestionValue : " + currentQuestionValue);
  console.log("selectedOption : " + selectedOption);

  var userOptedAnswersJSON = document.getElementById("userOptedAnswers").value;
  var userOptedAnswersArray = JSON.parse(userOptedAnswersJSON);
  userOptedAnswersArray[currentQuestionValue - 1] = selectedOption;

  var userOptedAnswersJSONNew = JSON.stringify(userOptedAnswersArray);
  document.getElementById("userOptedAnswers").value = userOptedAnswersJSONNew;
  console.log("userOptedAnswers new : " + document.getElementById("userOptedAnswers").value);

  showExistingSelectedOption(currentQuestionValue);
}


function approvedQuestionPage() {
  hideApproveButtons();
  showDiscardButtons();
  handleClick("APPROVED");
}

function discardedQuestionPage() {
  showApproveButtons();
  hideDiscardButtons();
  handleClick("DISCARD");
}

function newQuestionPage() {
  showApproveButtons();
  showDiscardButtons();
  handleClick("NEW");
}

/**
 * taking user input when he clicks the options 
 */
function hideApproveButtons() {
  console.log("Function hideApproveButton");
  var allApproveButtons = document.getElementsByClassName("approve-button");
  console.log("Function hideApproveButton allApproveButtons : " + allApproveButtons);
  for (let i = 0; i < allApproveButtons.length; i++) {
    allApproveButtons[i].style.display = "none";
  }
}


function showApproveButtons() {
  var allApproveButtons = document.getElementsByClassName("approve-button");
  for (let i = 0; i < allApproveButtons.length; i++) {
    allApproveButtons[i].style.removeProperty("display");
  }
}


/**
 * taking user input when he clicks the options 
 */
function hideDiscardButtons() {
  console.log("Function hideApproveButton");
  var allApproveButtons = document.getElementsByClassName("discard-button");
  console.log("Function hideApproveButton allApproveButtons : " + allApproveButtons);
  for (let i = 0; i < allApproveButtons.length; i++) {
    allApproveButtons[i].style.display = "none";
  }
}



function showDiscardButtons() {
  var allApproveButtons = document.getElementsByClassName("discard-button");
  for (let i = 0; i < allApproveButtons.length; i++) {
    allApproveButtons[i].style.removeProperty("display");
  }
}


function approveQuestion(questionId) {
  console.log("questionId : " + questionId);
  document.getElementById("questionId").value = questionId;
  document.getElementById("approvalLevel").value = 'APPROVED';
  document.getElementById("changeCategorySubmitForm").submit();
}


function discardQuestion(questionId) {
  console.log("questionId : " + questionId);
  document.getElementById("questionId").value = questionId;
  document.getElementById("approvalLevel").value = 'DISCARD';
  document.getElementById("changeCategorySubmitForm").submit();
}


function editQuestion(questionId) {
  console.log("questionId : " + questionId);
  document.getElementById("questionId").value = questionId;
  document.getElementById("approvalLevel").value = 'EDIT';
  document.getElementById("changeCategorySubmitForm").submit();
}




function updateQueryStringParameter(uri, key, value) {
  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
  var separator = uri.indexOf('?') !== -1 ? "&" : "?";
  if (uri.match(re)) {
    return uri.replace(re, '$1' + key + "=" + value + '$2');
  } else {
    return uri + separator + key + "=" + value;
  }
}

function handleClick(approvalLevel) {

  var currentUrl = window.location.href;
  console.log("currentUrl : " + currentUrl);

  var newUrl = updateQueryStringParameter(currentUrl, "approvalLevel", approvalLevel);
  console.log("newUrl : " + newUrl);

  window.location.replace(newUrl);

  display("Clicked, new value = " + cb.checked);
}


function showProperButtons() {

  let params = (new URL(document.location)).searchParams;
  console.log("query parameter searching ");
  let approvalLevel = params.get("approvalLevel");
  console.log("query parameter is " + approvalLevel);


  if (approvalLevel == "APPROVED") {
    console.log("approval level is approved");
    hideApproveButtons();
    showDiscardButtons();
    document.getElementById('radioButtonApproved').checked = true;
    document.getElementById('radioButtonNeedApproval').checked = false;
    document.getElementById('radioButtonDiscarded').checked = false;
  }
  else if (approvalLevel == "DISCARD") {
    console.log("approval level is discard");
    showApproveButtons();
    hideDiscardButtons();
    document.getElementById('radioButtonApproved').checked = false;
    document.getElementById('radioButtonNeedApproval').checked = false;
    document.getElementById('radioButtonDiscarded').checked = true;
  }
  else if (approvalLevel == "NEW") {
    console.log("approval level is new");
    showApproveButtons();
    showDiscardButtons();
    document.getElementById('radioButtonApproved').checked = false;
    document.getElementById('radioButtonNeedApproval').checked = true;
    document.getElementById('radioButtonDiscarded').checked = false;
  } else {
    console.log("approval level is null");
    showApproveButtons();
    showDiscardButtons();
    document.getElementById('radioButtonApproved').checked = false;
    document.getElementById('radioButtonNeedApproval').checked = true;
    document.getElementById('radioButtonDiscarded').checked = false;
  }

}




// window.onload = function () {
//   var reloading = sessionStorage.getItem("reloading");
//   if (reloading) {
//     sessionStorage.removeItem("reloading");
//     showProperButtons();
//   }
// }
