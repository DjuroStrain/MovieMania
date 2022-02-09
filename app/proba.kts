val adapter = ArrayAdapter(
        requireContext(), android.R.layout.simple_spinner_dropdown_item, arraylistOf("All Types", "Assignments", "Exam", "Lab")
)
binding?.typesFilterSpinner?.setAdapter(adapter)
binding?.typesFilterSpinner?.setText("All Types")