import streamlit as st
import pandas as pd
from datetime import datetime, timedelta
import random
import re

# Page configuration
st.set_page_config(
    page_title="IRCTC Ticket Booking",
    page_icon="🚆",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# CSS for modern styling
st.markdown("""
<style>
    .main-header {
        font-size: 3rem;
        color: #1f77b4;
        text-align: center;
        margin-bottom: 2rem;
        font-weight: bold;
    }
    .sub-header {
        font-size: 1.5rem;
        color: #2e86ab;
        margin-bottom: 1rem;
    }
    .success-message {
        background-color: #d4edda;
        color: #155724;
        padding: 15px;
        border-radius: 5px;
        border: 1px solid #c3e6cb;
        margin: 10px 0;
    }
    .train-card {
        background-color: #f8f9fa;
        padding: 15px;
        border-radius: 10px;
        border-left: 5px solid #1f77b4;
        margin: 10px 0;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .booking-card {
        background-color: #e8f4fd;
        padding: 20px;
        border-radius: 10px;
        border-left: 5px solid #28a745;
        margin: 10px 0;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .nav-button {
        background-color: #1f77b4;
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 5px;
        cursor: pointer;
        margin: 5px;
    }
    .nav-button:hover {
        background-color: #2e86ab;
    }
</style>
""", unsafe_allow_html=True)

# Sample data with more realistic train information
STATIONS = ["Mumbai", "Delhi", "Pune", "Chennai", "Kolkata", "Bangalore", "Hyderabad", "Ahmedabad"]
TRAIN_CLASSES = ["Sleeper", "AC 3 Tier", "AC 2 Tier", "AC First Class", "General"]

# Enhanced train database with realistic train names and numbers
TRAIN_DATABASE = {
    "Mumbai-Delhi": [
        {"Train No": "12951", "Train Name": "MUMBAI RAJDHANI", "Departure": "16:55", "Arrival": "08:35", 
         "Duration": "15h 40m", "Available": 45, "Classes": ["AC 2 Tier", "AC 3 Tier", "AC First Class"]},
        {"Train No": "12263", "Train Name": "DURONTO EXPRESS", "Departure": "21:15", "Arrival": "11:40", 
         "Duration": "14h 25m", "Available": 67, "Classes": ["AC 2 Tier", "AC 3 Tier"]},
        {"Train No": "12137", "Train Name": "PUNJAB MAIL", "Departure": "14:45", "Arrival": "10:30", 
         "Duration": "19h 45m", "Available": 128, "Classes": ["Sleeper", "AC 3 Tier", "AC 2 Tier"]}
    ],
    "Delhi-Mumbai": [
        {"Train No": "12952", "Train Name": "MUMBAI RAJDHANI", "Departure": "16:30", "Arrival": "08:15", 
         "Duration": "15h 45m", "Available": 52, "Classes": ["AC 2 Tier", "AC 3 Tier", "AC First Class"]},
        {"Train No": "12264", "Train Name": "DURONTO EXPRESS", "Departure": "20:45", "Arrival": "11:20", 
         "Duration": "14h 35m", "Available": 34, "Classes": ["AC 2 Tier", "AC 3 Tier"]}
    ],
    "Mumbai-Pune": [
        {"Train No": "12123", "Train Name": "DECCAN EXPRESS", "Departure": "07:15", "Arrival": "10:45", 
         "Duration": "3h 30m", "Available": 89, "Classes": ["Sleeper", "AC Chair Car"]},
        {"Train No": "12127", "Train Name": "INTERCITY EXPRESS", "Departure": "14:20", "Arrival": "17:50", 
         "Duration": "3h 30m", "Available": 76, "Classes": ["AC Chair Car", "General"]}
    ],
    "Delhi-Kolkata": [
        {"Train No": "12301", "Train Name": "HOWRAH RAJDHANI", "Departure": "16:55", "Arrival": "10:05", 
         "Duration": "17h 10m", "Available": 23, "Classes": ["AC 2 Tier", "AC 3 Tier", "AC First Class"]},
        {"Train No": "12305", "Train Name": "POORVA EXPRESS", "Departure": "15:15", "Arrival": "16:30", 
         "Duration": "25h 15m", "Available": 156, "Classes": ["Sleeper", "AC 3 Tier", "AC 2 Tier"]}
    ],
    "Chennai-Bangalore": [
        {"Train No": "12607", "Train Name": "LALBAGH EXPRESS", "Departure": "06:15", "Arrival": "11:25", 
         "Duration": "5h 10m", "Available": 112, "Classes": ["Sleeper", "AC Chair Car"]},
        {"Train No": "12007", "Train Name": "SHATABDI EXPRESS", "Departure": "14:30", "Arrival": "19:05", 
         "Duration": "4h 35m", "Available": 45, "Classes": ["AC Chair Car"]}
    ]
}

# Class-wise pricing
CLASS_PRICES = {
    "Sleeper": 500,
    "AC 3 Tier": 1200,
    "AC 2 Tier": 1800,
    "AC First Class": 2500,
    "General": 300,
    "AC Chair Car": 800
}

def generate_dummy_trains(source, destination):
    """Generate train data for the given route"""
    route_key = f"{source}-{destination}"
    
    if route_key in TRAIN_DATABASE:
        return TRAIN_DATABASE[route_key]
    else:
        # Generate random trains for routes not in database
        trains = []
        for i in range(3):
            train_no = f"12{random.randint(100, 999)}"
            departure = f"{random.randint(6, 22):02d}:{random.randint(0, 59):02d}"
            arrival_hour = (int(departure[:2]) + random.randint(4, 10)) % 24
            arrival = f"{arrival_hour:02d}:{random.randint(0, 59):02d}"
            
            trains.append({
                "Train No": train_no,
                "Train Name": f"{source}-{destination} Express {i+1}",
                "Departure": departure,
                "Arrival": arrival,
                "Duration": f"{random.randint(4, 10)}h {random.randint(0, 59)}m",
                "Available": random.randint(10, 100),
                "Classes": random.sample(TRAIN_CLASSES, random.randint(2, 4))
            })
        return trains

def calculate_fare(train_class, base_distance=500):
    """Calculate fare based on class and distance"""
    base_price = CLASS_PRICES.get(train_class, 500)
    return base_price + (base_distance // 100) * 50

def validate_email(email):
    """Validate email format"""
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(pattern, email) is not None

def validate_phone(phone):
    """Validate phone number"""
    pattern = r'^[6-9]\d{9}$'
    return re.match(pattern, phone) is not None

def initialize_session_state():
    """Initialize session state variables"""
    if 'page' not in st.session_state:
        st.session_state.page = "search"
    if 'selected_train' not in st.session_state:
        st.session_state.selected_train = None
    if 'booking_details' not in st.session_state:
        st.session_state.booking_details = None
    if 'my_bookings' not in st.session_state:
        st.session_state.my_bookings = []
    if 'available_trains' not in st.session_state:
        st.session_state.available_trains = []
    if 'journey_details' not in st.session_state:
        st.session_state.journey_details = {}

def main():
    # Initialize session state
    initialize_session_state()
    
    # Main header
    st.markdown('<div class="main-header">🚆 IRCTC Ticket Booking</div>', unsafe_allow_html=True)
    
    # Sidebar for navigation
    with st.sidebar:
        st.header("Navigation")
        if st.button("🏠 Home", use_container_width=True):
            st.session_state.page = "search"
            st.session_state.selected_train = None
            st.session_state.booking_details = None
        
        if st.button("🔍 Search Trains", use_container_width=True):
            st.session_state.page = "search"
        
        if st.button("📋 My Bookings", use_container_width=True):
            st.session_state.page = "bookings"
        
        # Display current bookings count
        if st.session_state.my_bookings:
            st.sidebar.markdown(f"**Current Bookings: {len(st.session_state.my_bookings)}**")
    
    # Page routing
    if st.session_state.page == "search":
        show_search_page()
    elif st.session_state.page == "booking":
        show_booking_page()
    elif st.session_state.page == "bookings":
        show_my_bookings()
    elif st.session_state.page == "confirmation":
        show_confirmation_page()

def show_search_page():
    """Show train search page"""
    st.markdown('<div class="sub-header">🔍 Search Trains</div>', unsafe_allow_html=True)
    
    # Search form
    col1, col2, col3, col4 = st.columns(4)
    
    with col1:
        source = st.selectbox("Source Station", STATIONS, index=0, key="source_station")
    
    with col2:
        destination = st.selectbox("Destination Station", STATIONS, index=1, key="destination_station")
    
    with col3:
        journey_date = st.date_input("Date of Journey", 
                                   min_value=datetime.today(),
                                   max_value=datetime.today() + timedelta(days=60),
                                   key="journey_date")
    
    with col4:
        travel_class = st.selectbox("Class Preference", ["Any"] + TRAIN_CLASSES, key="travel_class")
    
    # Search button
    if st.button("🔍 Search Trains", use_container_width=True, type="primary"):
        if source == destination:
            st.error("❌ Source and destination cannot be the same!")
            return
        
        # Generate and display trains
        trains = generate_dummy_trains(source, destination)
        
        # Filter by class if specified
        if travel_class != "Any":
            trains = [train for train in trains if travel_class in train.get("Classes", TRAIN_CLASSES)]
        
        st.session_state.available_trains = trains
        st.session_state.journey_details = {
            "source": source,
            "destination": destination,
            "date": journey_date,
            "class_preference": travel_class
        }
        
        st.success(f"✅ Found {len(trains)} trains for {source} to {destination} on {journey_date.strftime('%d %b, %Y')}")
    
    # Display available trains if any
    if st.session_state.available_trains:
        st.markdown(f'<div class="sub-header">🚄 Available Trains</div>', unsafe_allow_html=True)
        
        for i, train in enumerate(st.session_state.available_trains):
            with st.container():
                st.markdown(f'<div class="train-card">', unsafe_allow_html=True)
                
                col1, col2, col3, col4 = st.columns([3, 2, 2, 1])
                
                with col1:
                    st.write(f"**{train['Train Name']}**")
                    st.write(f"Train No: {train['Train No']}")
                    st.write(f"Available Classes: {', '.join(train.get('Classes', TRAIN_CLASSES))}")
                
                with col2:
                    st.write(f"**🕒 Departure:** {train['Departure']}")
                    st.write(f"**🕒 Arrival:** {train['Arrival']}")
                    st.write(f"**⏱ Duration:** {train['Duration']}")
                
                with col3:
                    st.write(f"**Seats Available:** {train['Available']}")
                    # Show approximate fare range
                    if 'Classes' in train:
                        min_fare = min(CLASS_PRICES[cls] for cls in train['Classes'] if cls in CLASS_PRICES)
                        max_fare = max(CLASS_PRICES[cls] for cls in train['Classes'] if cls in CLASS_PRICES)
                        st.write(f"**Fare:** ₹{min_fare} - ₹{max_fare}")
                
                with col4:
                    if st.button(f"Book Now", key=f"book_{i}", use_container_width=True):
                        st.session_state.selected_train = train
                        st.session_state.page = "booking"
                        st.rerun()
                
                st.markdown('</div>', unsafe_allow_html=True)

def show_booking_page():
    """Show booking page"""
    if not st.session_state.selected_train:
        st.error("No train selected! Please go back and select a train.")
        if st.button("← Back to Search"):
            st.session_state.page = "search"
            st.rerun()
        return
    
    train = st.session_state.selected_train
    journey = st.session_state.journey_details
    
    st.markdown('<div class="sub-header">🎫 Book Ticket</div>', unsafe_allow_html=True)
    
    # Display train details
    col1, col2 = st.columns(2)
    
    with col1:
        st.markdown("### Train Details")
        st.info(f"""
        **Train Information:**
        - **Train:** {train['Train Name']}
        - **Number:** {train['Train No']}
        - **Route:** {journey['source']} → {journey['destination']}
        - **Date:** {journey['date'].strftime('%d %b, %Y')}
        - **Time:** {train['Departure']} - {train['Arrival']}
        - **Duration:** {train['Duration']}
        - **Seats Available:** {train['Available']}
        """)
    
    # Passenger details form
    with col2:
        st.markdown("### Passenger Details")
        passenger_name = st.text_input("Full Name *", placeholder="Enter full name as per ID proof")
        
        col_age, col_gender = st.columns(2)
        with col_age:
            age = st.number_input("Age *", min_value=1, max_value=100, value=25)
        with col_gender:
            gender = st.selectbox("Gender *", ["Male", "Female", "Other"])
        
        # Available classes for this train
        available_classes = train.get("Classes", TRAIN_CLASSES)
        train_class = st.selectbox("Class *", available_classes)
        
        berth_preference = st.selectbox("Berth Preference", ["No Preference", "Lower", "Middle", "Upper", "Side Lower", "Side Upper"])
        
        # Calculate and display fare
        fare = calculate_fare(train_class)
        st.markdown(f"**Estimated Fare: ₹{fare}**")
    
    # Contact details
    st.markdown("### Contact Information")
    col3, col4 = st.columns(2)
    with col3:
        email = st.text_input("Email *", placeholder="your.email@example.com")
    with col4:
        phone = st.text_input("Mobile Number *", placeholder="10-digit mobile number")
    
    # Terms and conditions
    st.markdown("### Terms & Conditions")
    terms_accepted = st.checkbox("I agree to the terms and conditions *")
    
    # Payment and booking buttons
    st.markdown("---")
    col5, col6 = st.columns(2)
    
    with col5:
        if st.button("← Back to Search", use_container_width=True):
            st.session_state.page = "search"
            st.rerun()
    
    with col6:
        if st.button("💳 Confirm Booking", use_container_width=True, type="primary"):
            # Validate inputs
            if not all([passenger_name, email, phone]):
                st.error("❌ Please fill all required fields!")
                return
            
            if not validate_email(email):
                st.error("❌ Please enter a valid email address!")
                return
            
            if not validate_phone(phone):
                st.error("❌ Please enter a valid 10-digit mobile number!")
                return
            
            if not terms_accepted:
                st.error("❌ Please accept the terms and conditions!")
                return
            
            # Save booking
            booking_id = f"IRCTC{random.randint(100000, 999999)}"
            fare = calculate_fare(train_class)
            
            booking_details = {
                "booking_id": booking_id,
                "train": train,
                "journey": journey,
                "passenger": {
                    "name": passenger_name,
                    "age": age,
                    "gender": gender,
                    "class": train_class,
                    "berth": berth_preference
                },
                "contact": {
                    "email": email,
                    "phone": phone
                },
                "fare": fare,
                "booking_time": datetime.now(),
                "status": "Confirmed"
            }
            
            # Save to session state
            st.session_state.my_bookings.append(booking_details)
            st.session_state.booking_details = booking_details
            
            st.session_state.page = "confirmation"
            st.rerun()

def show_my_bookings():
    """Show user's bookings"""
    st.markdown('<div class="sub-header">📋 My Bookings</div>', unsafe_allow_html=True)
    
    if not st.session_state.my_bookings:
        st.info("No bookings found. Book your first ticket!")
        return
    
    # Filter options
    col1, col2 = st.columns(2)
    with col1:
        show_active = st.checkbox("Show only active bookings", value=True)
    with col2:
        if st.button("Clear All Bookings", type="secondary"):
            st.session_state.my_bookings = []
            st.success("All bookings cleared!")
            st.rerun()
    
    # Display bookings
    for i, booking in enumerate(st.session_state.my_bookings):
        if show_active and booking.get("status") == "Cancelled":
            continue
            
        with st.container():
            status_color = "green" if booking.get("status") == "Confirmed" else "red"
            
            st.markdown(f"""
            <div class="booking-card">
    <h4 style="color: #000000;">🎫 Booking ID: {booking['booking_id']}</h4>
    <p style="color: #000000;"><strong style="color: #000000;">Train:</strong> {booking['train']['Train Name']} ({booking['train']['Train No']})</p>
    <p style="color: #000000;"><strong style="color: #000000;">Route:</strong> {booking['journey']['source']} → {booking['journey']['destination']}</p>
    <p style="color: #000000;"><strong style="color: #000000;">Date:</strong> {booking['journey']['date'].strftime('%d %b, %Y')}</p>
    <p style="color: #000000;"><strong style="color: #000000;">Time:</strong> {booking['train']['Departure']}</p>
    <p style="color: #000000;"><strong style="color: #000000;">Passenger:</strong> {booking['passenger']['name']} | {booking['passenger']['class']}</p>
    <p style="color: #000000;"><strong style="color: #000000;">Fare:</strong> ₹{booking['fare']}</p>
    <p style="color: #000000;"><strong style="color: #000000;">Status:</strong> <span style='color: {status_color};'>{booking.get('status', 'Confirmed')}</span></p>
    <p style="color: #000000;"><strong style="color: #000000;">Booked on:</strong> {booking['booking_time'].strftime('%d %b, %Y %H:%M')}</p>
</div>
            """, unsafe_allow_html=True)
            
            col1, col2, col3 = st.columns(3)
            
            with col1:
                if st.button(f"Download Ticket", key=f"download_{i}"):
                    st.success(f"📥 Ticket {booking['booking_id']} downloaded successfully!")
            
            with col2:
                if st.button(f"Email Ticket", key=f"email_{i}"):
                    st.success(f"📧 Ticket sent to {booking['contact']['email']}")
            
            with col3:
                if booking.get("status") == "Confirmed":
                    if st.button(f"Cancel Booking", key=f"cancel_{i}", type="secondary"):
                        booking["status"] = "Cancelled"
                        st.success("✅ Booking cancelled successfully!")
                        st.rerun()
            
            st.markdown("---")

def show_confirmation_page():
    """Show booking confirmation page"""
    if not st.session_state.booking_details:
        st.error("No booking details found!")
        st.session_state.page = "search"
        st.rerun()
        return
    
    booking = st.session_state.booking_details
    
    st.markdown('<div class="success-message">🎉 Ticket Booked Successfully!</div>', unsafe_allow_html=True)
    
    col1, col2 = st.columns(2)
    
    with col1:
        st.markdown("### Booking Details")
        st.info(f"""
        **Booking Information:**
        - **Booking ID:** {booking['booking_id']}
        - **Train:** {booking['train']['Train Name']}
        - **Train No:** {booking['train']['Train No']}
        - **Route:** {booking['journey']['source']} → {booking['journey']['destination']}
        - **Date:** {booking['journey']['date'].strftime('%d %b, %Y')}
        - **Time:** {booking['train']['Departure']}
        - **Duration:** {booking['train']['Duration']}
        - **Fare Paid:** ₹{booking['fare']}
        """)
    
    with col2:
        st.markdown("### Passenger Details")
        st.info(f"""
        **Passenger Information:**
        - **Name:** {booking['passenger']['name']}
        - **Age:** {booking['passenger']['age']}
        - **Gender:** {booking['passenger']['gender']}
        - **Class:** {booking['passenger']['class']}
        - **Berth Preference:** {booking['passenger']['berth']}
        
        **Contact:**
        - **Email:** {booking['contact']['email']}
        - **Phone:** {booking['contact']['phone']}
        """)
    
    # Action buttons
    st.markdown("---")
    col3, col4, col5 = st.columns(3)
    
    with col3:
        if st.button("🏠 Book Another Ticket", use_container_width=True):
            st.session_state.page = "search"
            st.session_state.selected_train = None
            st.rerun()
    
    with col4:
        if st.button("📋 View My Bookings", use_container_width=True):
            st.session_state.page = "bookings"
            st.rerun()
    
    with col5:
        if st.button("📥 Download Ticket", use_container_width=True, type="primary"):
            st.success(f"✅ Ticket {booking['booking_id']} downloaded successfully!")

# Run the application
if __name__ == "__main__":
    main()