# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/xenial64"

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.vm.provision "shell" do |shell|
    ssh_pub_key = File.readlines("public_key").first.strip

    shell.inline = <<-SHELL
        echo #{ssh_pub_key} >> /home/ubuntu/.ssh/authorized_keys
        apt-get update
        apt-get install -y python
    SHELL
  end

  {:production => '192.168.33.10', :jenkins => '192.168.32.10'}.each do |name, ip|
    config.vm.define name do |machine|

      machine.vm.network "private_network", ip: ip

      machine.vm.provision "ansible" do |ansible|
        ansible.playbook = "site.yml"
        ansible.inventory_path = "hosts"
        ansible.limit = "vagrant-#{name}"
      end
    end

  end
end
