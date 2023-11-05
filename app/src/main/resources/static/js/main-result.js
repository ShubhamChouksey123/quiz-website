(function () {
  "use strict";

  document.getElementById("submit_quiz_request_button").addEventListener("click", onClickSubmitButtonRequest);

  document.getElementById("nextQuestionButton").addEventListener("click", onClickNextQuestionButton);
  document.getElementById("previousQuestionButton").addEventListener("click", onClickPreviousQuestionButton);

  /**
 * Defaults for initial first question
 */
  setOptionNullSelected();

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
   * Initiate portfolio lightbox
   */
  const portfolioLightbox = GLightbox({
    selector: '.portfolio-lightbox'
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

  /**
   * Animation on scroll
   */
  window.addEventListener('load', () => {
    AOS.init({
      duration: 1000,
      easing: 'ease-in-out',
      once: true,
      mirror: false
    })
  });

  /**
   * Initiate Pure Counter
   */
  new PureCounter();

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



function setOptionNullSelected() {
  let totalQuestionValue = Number(document.getElementById("totalQuestions").value);
  console.log("totalQuestionValue : " + totalQuestionValue);

  var userOptedAnswersArray = Array.from(Array(totalQuestionValue));

  var userOptedAnswersJSON = JSON.stringify(userOptedAnswersArray);
  document.getElementById("userOptedAnswers").value = userOptedAnswersJSON;

  console.log("userOptedAnswers : " + document.getElementById("userOptedAnswers").value);
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

function showExistingSelectedOption(currentQuestion) {

  var userOptedAnswersJSON = document.getElementById("userOptedAnswers").value;
  var userOptedAnswersArray = JSON.parse(userOptedAnswersJSON);
  var selectedOption = userOptedAnswersArray[currentQuestion - 1];

  document.getElementById("optionALabel").style.background = "null";
  document.getElementById("optionBLabel").style.background = "null";
  document.getElementById("optionCLabel").style.background = "null";
  document.getElementById("optionDLabel").style.background = "null";

  document.getElementById("optionALabel").style.removeProperty('background');
  document.getElementById("optionBLabel").style.removeProperty('background');
  document.getElementById("optionCLabel").style.removeProperty('background');
  document.getElementById("optionDLabel").style.removeProperty('background');

  if (selectedOption === 0) {
    document.getElementById("optionALabel").style.background = "#fa2a00";
  }
  else if (selectedOption === 1) {
    document.getElementById("optionBLabel").style.background = "#fa2a00";
  }
  else if (selectedOption === 2) {
    document.getElementById("optionCLabel").style.background = "#fa2a00";
  }
  else if (selectedOption === 3) {
    document.getElementById("optionDLabel").style.background = "#fa2a00";
  }

}

/**
 * 
 * function used to change the page based on the question number reached. 
 *  it changes the next or submit button, question text and information on the page
 */
function showDesiredQuestionPage(currentQuestion, totalQuestion) {
  showDesiredButton(currentQuestion, totalQuestion);
  showDesiredNumberQuestion(Number(currentQuestion - 1));
  showDesiredNumberOnInfo(currentQuestion, totalQuestion);
  showExistingSelectedOption(currentQuestion);

}


/**
 * for changing the current Shown Question Number to next page
 */
function onClickNextQuestionButton() {
  var elementCurrentQuestionNumber = document.getElementById("questionNumberToShow");
  let currentQuestionValue = Number(document.getElementById("questionNumberToShow").value);
  let totalQuestionValue = Number(document.getElementById("totalQuestions").value);

  if (currentQuestionValue < totalQuestionValue) {
    elementCurrentQuestionNumber.value = currentQuestionValue + 1;
    showDesiredQuestionPage(elementCurrentQuestionNumber.value, totalQuestionValue);
    hideSubmitQuizForm();
  }
  else if (currentQuestionValue == totalQuestionValue) {
    onClickSubmitButtonRequest();
  }
  console.log("elementCurrentQuestionNumber.value : " + elementCurrentQuestionNumber.value);
}

/**
 * for changing the current Shown Question Number to previous page
 */
function onClickPreviousQuestionButton() {
  var elementCurrentQuestionNumber = document.getElementById("questionNumberToShow");
  let currentQuestionValue = Number(document.getElementById("questionNumberToShow").value);

  let totalQuestionValue = Number(document.getElementById("totalQuestions").value);

  if (currentQuestionValue > 1) {
    elementCurrentQuestionNumber.value = currentQuestionValue - 1;
    showDesiredQuestionPage(elementCurrentQuestionNumber.value, totalQuestionValue);
  }
  hideSubmitQuizForm();
  console.log("elementCurrentQuestionNumber.value : " + elementCurrentQuestionNumber.value);
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


function submitQuizForm() {

  console.log("userName : " + document.getElementById("userName").value);
  console.log("userEmail : " + document.getElementById("userEmail").value);
  document.getElementById("quizSubmitForm").submit();
}

