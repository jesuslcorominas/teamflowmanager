import SwiftUI
import shared

struct ContentView: View {
    let greeting = Greeting()
    
    var body: some View {
        VStack {
            Image(systemName: "sportscourt")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text(greeting.greet())
                .padding()
            Text("TeamFlow Manager")
                .font(.title)
                .fontWeight(.bold)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
