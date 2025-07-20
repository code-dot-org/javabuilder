require 'erb'

# Read the template
template_content = File.read('../cicd/3-app/javabuilder/template.yml.erb')

# Replace SSL certificate conditions to always be false
template_content.gsub!(/CreateSSLCertificates: !Not \[Condition: IsDevCondition\]/, 'CreateSSLCertificates: false')

# Add condition to skip SSL resources
ssl_skip_condition = <<~CONDITION
  SkipSSLResources: true
CONDITION

# Insert after existing conditions
template_content.sub!(/^Conditions:.*?\n/, "Conditions:\n#{ssl_skip_condition}")

# Wrap SSL certificate resources with condition
ssl_resources = [
  'HttpCertificate',
  'WebSocketCertificate',
  'HttpDomainName', 
  'WebSocketDomainName',
  'HttpDomain',
  'WebSocketDomain',
  'HttpDomainNameApiMapping',
  'WebSocketDomainNameApiMapping'
]

ssl_resources.each do |resource|
  template_content.gsub!(/^  #{resource}:/, "  #{resource}:\n    Condition: CreateSSLCertificates")
end

# Process the ERB template
erb = ERB.new(template_content)
processed = erb.result

# Write processed template
File.write('template-no-ssl.yml', processed)
puts "Template processed successfully"
