# Java_HealthcareRobots_Simulation

## Overview

Java Healthcare Robots Simulation is a Java-based project that models and simulates the behavior of healthcare robots operating in a medical environment. The simulation demonstrates how autonomous robots can assist healthcare facilities by performing tasks such as patient monitoring, medication delivery, room assistance, and resource management.

The project aims to provide a practical example of object-oriented programming concepts, simulation design, and healthcare automation technologies.

## Features

* Simulation of multiple healthcare robots
* Patient and medical staff interaction
* Task scheduling and assignment
* Autonomous robot movement and decision-making
* Resource and workload management
* Event-driven simulation environment
* Modular and extensible architecture

## Technologies Used

* Java
* Object-Oriented Programming (OOP)
* Collections Framework
* Exception Handling

## 🖥️ Graphical User Interface

The GUI is built using Java Swing.

### Main Components

#### DashboardPanel

Provides:

* Hospital map visualization
* Robot monitoring
* Dashboard information

#### CommandPanel

Allows:

* Mission creation
* User interactions
* Fleet control

#### LogPanel

Displays:

* Real-time events
* Mission logs
* Robot activities

## 📂 Project Structure

```text
src/
├── robots/
│   ├── Robot.java
│   ├── RobotConnecte.java
│   ├── RobotLivraison.java
│   └── RobotCompagnon.java
│
├── missions/
│   ├── Mission.java
│   ├── MissionLivraison.java
│   └── MissionCompagnon.java
│
├── management/
│   ├── GestionnaireHospitalier.java
│   └── SecurityService.java
│
├── map/
│   └── PlanHopital.java
│
├── gui/
│   ├── MainFrame.java
│   ├── DashboardPanel.java
│   ├── CommandPanel.java
│   └── LogPanel.java
│
└── Main.java
```

## System Components
### Core Classes

#### Robot

Base abstract class containing:

* Robot ID
* Position coordinates
* Energy level
* Usage hours
* Action history

#### RobotConnecte

Extends Robot and implements network communication features through the Connectable interface.

#### RobotLivraison

Handles:

* Logistics missions
* Secure transportation
* Route optimization

#### RobotCompagnon

Handles:

* Patient assistance
* Stress analysis
* Emotional support services

#### GestionnaireHospitalier

Acts as the central controller:

* Fleet management
* Mission assignment 
* Robot selection
* System supervision

#### SecurityService

Provides:

* Access validation
* Mission protection
* Security controls

#### PlanHopital

Manages:

* Hospital map
* Navigation
* Distance calculations

### Patients

Patients are represented as simulation entities with specific attributes such as:

* Name
* Identifier
* Health status
* Assigned tasks

### Task Management

The task management system:

* Creates healthcare tasks
* Assigns tasks to available robots
* Tracks task completion
* Generates reports

### Simulation Engine

The simulation engine controls:

* Environment initialization
* Robot behavior execution
* Event processing
* Simulation timing

## Getting Started

### Prerequisites

* Java JDK 17 or later
* IDE such as IntelliJ IDEA, Eclipse, or VS Code

### Installation

1. Clone the repository:

```bash
git clone https://github.com/your-username/Java_HealthcareRobots_Simulation.git
```

2. Navigate to the project directory:

```bash
cd Java_HealthcareRobots_Simulation
```

3. Compile the project:

```bash
javac -d bin src/**/*.java
```

4. Run the application:

```bash
java -cp bin simulation.Main
```

## Example Workflow

1. Initialize the healthcare environment.
2. Register patients and healthcare robots.
3. Create healthcare tasks.
4. Assign tasks to available robots.
5. Execute the simulation cycle.
6. Display results and statistics.

## Learning Objectives

This project demonstrates:

* Object-Oriented Design
* Inheritance and Polymorphism
* Encapsulation and Abstraction
* Simulation Modeling
* Java Collections
* Software Engineering Principles

## Future Improvements

* Graphical User Interface (GUI)
* Database integration
* Real-time monitoring dashboard
* Advanced path-finding algorithms
* Multi-hospital simulation support

## 👥 Authors

* [SABRINE BEN TILI](https://www.linkedin.com/in/sabrine-ben-tili/) [@girll235](https://github.com/girll235/)
* [MOHAMED AMMOUS](https://www.facebook.com/mouhamed.ammous) [@MohamedAmmous](https://github.com/MohamedAmmous)

## 📜 License

This project is distributed under the MIT License. See the LICENSE file for additional information.
